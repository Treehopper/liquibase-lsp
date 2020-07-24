package eu.hohenegger.lemminx.adapter.impl;

import org.apache.xerces.xni.XMLLocator;

import eu.hohenegger.lemminx.xerces.wrapper.LMXLocator;

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

    public XMLLocator getLocator() {
        return locator;
    }
    
    @Override
    public int getCharacterOffset() {
        return getLocator().getCharacterOffset();
    }

}
