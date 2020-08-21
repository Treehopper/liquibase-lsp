package eu.hohenegger.lemminx.xerces.wrapper;

import org.apache.xerces.xni.parser.XMLParseException;

public class LMXParseException extends LMXXNIException {

    private static final long serialVersionUID = -2393959177927508111L;
    
    private XMLParseException xmlParseException;

    public LMXParseException(LMXLocator location, String message, Exception exception) {
        super(message, exception);
        xmlParseException = new XMLParseException(((XMLLocatorWrapper)location).getLocator(), message, exception);
    }

    public LMXParseException(LMXLocator location, String message) {
        super(message);
        xmlParseException = new XMLParseException(((XMLLocatorWrapper)location).getLocator(), message);
    }

}
