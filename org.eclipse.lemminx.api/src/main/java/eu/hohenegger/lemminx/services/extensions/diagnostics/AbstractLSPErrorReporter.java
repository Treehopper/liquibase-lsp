package eu.hohenegger.lemminx.services.extensions.diagnostics;

import static eu.hohenegger.lemminx.utils.wrapper.LMXPositionUtility.toLSPPosition;
import static org.apache.xerces.impl.XMLErrorReporter.SEVERITY_FATAL_ERROR;
import static org.apache.xerces.impl.XMLErrorReporter.SEVERITY_WARNING;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.xerces.impl.msg.XMLMessageFormatter;
import org.apache.xerces.impl.xs.XSMessageFormatter;
import org.apache.xerces.util.MessageFormatter;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.extensions.xerces.LSPMessageFormatter;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.DiagnosticSeverity;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.Range;

import eu.hohenegger.lemminx.adapter.impl.LMXErrorReporter;
import eu.hohenegger.lemminx.adapter.impl.MessageFormatterWrapper;
import eu.hohenegger.lemminx.xerces.wrapper.LMXLocator;
import eu.hohenegger.lemminx.xerces.wrapper.LMXParseException;
import eu.hohenegger.lemminx.xerces.wrapper.LMXXNIException;

/**
 * The SAX {@link ErrorHandler} gives just information of the offset where there
 * is an error. To improve highlight XML error, this class extends the Xerces
 * XML reporter to catch location, key, arguments which is helpful to adjust the
 * LSP range.
 *
 */
public abstract class AbstractLSPErrorReporter extends LMXErrorReporter {

    private final DOMDocument xmlDocument;
    private final List<Diagnostic> diagnostics;

    private final String source;
    
    private static final Logger LOGGER = Logger.getLogger(AbstractLSPErrorReporter.class.getName());

    public AbstractLSPErrorReporter(String source, DOMDocument xmlDocument, List<Diagnostic> diagnostics) {
        this.source = source;
        this.xmlDocument = xmlDocument;
        this.diagnostics = diagnostics;
        MessageFormatter xmft = new XMLMessageFormatter();
        super.putMessageFormatter(XMLMessageFormatter.XML_DOMAIN, xmft);
        super.putMessageFormatter(XMLMessageFormatter.XMLNS_DOMAIN, xmft);
        //FIXME: somehow LSPMessageFormatter breaks this initialization
//        super.putMessageFormatter(XSMessageFormatter.SCHEMA_DOMAIN, new LSPMessageFormatter());
    }

    public String reportError(LMXLocator location, String domain, String key, Object[] arguments, short severity,
            Exception exception) throws LMXXNIException {
        // format message
        MessageFormatterWrapper messageFormatter = getMessageFormatter(domain);
        String message;
        if (messageFormatter != null) {
            message = messageFormatter.formatMessage(getLocale(), key, arguments);
        } else {
            StringBuilder str = new StringBuilder();
            str.append(domain);
            str.append('#');
            str.append(key);
            int argCount = arguments != null ? arguments.length : 0;
            if (argCount > 0) {
                str.append('?');
                for (int i = 0; i < argCount; i++) {
                    str.append(arguments[i]);
                    if (i < argCount - 1) {
                        str.append('&');
                    }
                }
            }
            message = str.toString();
        }

        Range adjustedRange = internalToLSPRange(location, key, arguments, xmlDocument);

        if (adjustedRange == null) {
            return null;
        }

        if (!addDiagnostic(adjustedRange, message, toLSPSeverity(severity), key)) {
            return null;
        }

        if (severity == SEVERITY_FATAL_ERROR && !isContinueAfterFatalError() && !isIgnoreFatalError(key)) {
            LMXParseException parseException = (exception != null) ? new LMXParseException(location, message, exception)
                    : new LMXParseException(location, message);
            throw parseException;
        }
        return message;
    }

    protected boolean isIgnoreFatalError(String key) {
        return false;
    }

    public boolean addDiagnostic(Range adjustedRange, String message, DiagnosticSeverity severity, String key) {
        Diagnostic d = new Diagnostic(adjustedRange, message, severity, source, key);
        if (diagnostics.contains(d)) {
            return false;
        }
        // Fill diagnostic
        diagnostics.add(d);
        return true;
    }

    /**
     * Returns the LSP diagnostic severity according the SAX severity.
     * 
     * @param severity the SAX severity
     * @return the LSP diagnostic severity according the SAX severity.
     */
    private static DiagnosticSeverity toLSPSeverity(int severity) {
        switch (severity) {
        case SEVERITY_WARNING:
            return DiagnosticSeverity.Warning;
        default:
            return DiagnosticSeverity.Error;
        }
    }

    /**
     * Create the LSP range from the SAX error.
     * 
     * @param location
     * @param key
     * @param arguments
     * @param document
     * @return the LSP range from the SAX error.
     */
    private Range internalToLSPRange(LMXLocator location, String key, Object[] arguments, DOMDocument document) {
        if (location == null) {
            Position start = toLSPPosition(0, location, document.getTextDocument());
            Position end = toLSPPosition(0, location, document.getTextDocument());
            return new Range(start, end);
        }

        Range range = toLSPRange(location, key, arguments, document);
        if (range != null) {
            return range;
        }
        int startOffset = location.getCharacterOffset() - 1;
        int endOffset = location.getCharacterOffset() - 1;

        if (startOffset < 0 || endOffset < 0) {
            return null;
        }

        // Create LSP range
        Position start = toLSPPosition(startOffset, location, document.getTextDocument());
        Position end = toLSPPosition(endOffset, location, document.getTextDocument());
        return new Range(start, end);
    }

    protected abstract Range toLSPRange(LMXLocator location, String key, Object[] arguments, DOMDocument document);
}
