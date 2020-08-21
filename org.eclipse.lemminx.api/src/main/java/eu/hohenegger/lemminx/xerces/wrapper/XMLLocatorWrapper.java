package eu.hohenegger.lemminx.xerces.wrapper;

import org.apache.xerces.xni.XMLLocator;

public class XMLLocatorWrapper implements LMXLocator {
    
    private XMLLocator locator;

    public XMLLocatorWrapper(XMLLocator locator) {
        this.locator = locator;
    }

    @Override
    public int getLineNumber() {
        return getLocator().getLineNumber();
    }

    @Override
    public int getColumnNumber() {
        return getLocator().getColumnNumber();
    }

    XMLLocator getLocator() {
        return locator;
    }
    
    @Override
    public int getCharacterOffset() {
        return getLocator().getCharacterOffset();
    }

}
