package eu.hohenegger.lemminx.xerces.wrapper;

import org.apache.xerces.xni.XNIException;

public class LMXXNIException extends Exception {

    private XNIException xniException;

    public LMXXNIException(Exception exception) {
        xniException = new XNIException(exception);
    }

    public LMXXNIException(String message, Exception exception) {
        xniException = new XNIException(message, exception);
    }

    public LMXXNIException(String message) {
        xniException = new XNIException(message);
    }

}
