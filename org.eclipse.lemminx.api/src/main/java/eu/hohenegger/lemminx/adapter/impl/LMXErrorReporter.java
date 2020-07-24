package eu.hohenegger.lemminx.adapter.impl;

import java.util.Locale;

import org.apache.xerces.impl.Constants;
import org.apache.xerces.impl.XMLErrorReporter;
import org.apache.xerces.util.MessageFormatter;

public class LMXErrorReporter {
    private XMLErrorReporter xmlErrorReporter;

    public LMXErrorReporter() {
        xmlErrorReporter = new XMLErrorReporter();
    }

    public void putMessageFormatter(String domain, MessageFormatter messageFormatter) {
        xmlErrorReporter.putMessageFormatter(domain, messageFormatter);
    }

    public MessageFormatterWrapper getMessageFormatter(String domain) {
        return new MessageFormatterWrapper(xmlErrorReporter.getMessageFormatter(domain));
    }
    
    public Locale getLocale() {
        return xmlErrorReporter.getLocale();
    }
    
    public boolean isContinueAfterFatalError() {
        return xmlErrorReporter.getFeature(Constants.XERCES_FEATURE_PREFIX + Constants.CONTINUE_AFTER_FATAL_ERROR_FEATURE);
    }
}
