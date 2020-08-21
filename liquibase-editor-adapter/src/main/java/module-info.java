module liquibase.editor.adapter {
    
    exports liquibase.wrapper;
    exports liquibase.wrapper.exception;
    
    requires org.eclipse.lsp4j;
    
    requires lemminx.api;
    
    //ResolutionException: Modules xercesImpl and jdk.xml.dom export package org.w3c.dom.html to module org.eclipse.lsp4xml
    requires java.xml;
    
    requires liquibase.core;
    requires java.sql;
    requires java.naming;
    
    requires java.logging;
    requires jabel;
}
