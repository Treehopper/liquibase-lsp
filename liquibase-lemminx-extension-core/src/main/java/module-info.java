/*
 * #%L
 * %%
 * Copyright (C) 2020 Max Hohenegger <eclipse@hohenegger.eu>
 * %%
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 * 
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the Eclipse
 * Public License, v. 2.0 are satisfied: GNU General Public License, version 2
 * with the GNU Classpath Exception which is
 * available at https://www.gnu.org/software/classpath/license.html.
 * 
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 * #L%
 */
import org.eclipse.lemminx.liquibase.Plugin;
import org.eclipse.lemminx.services.extensions.IXMLExtension;

module liquibase.lsp {
    
    exports org.eclipse.lemminx.liquibase;
    
    requires org.eclipse.lemminx;
    requires org.eclipse.lsp4j;
    
    requires lemminx.api;
    requires liquibase.editor.adapter;
    requires org.eclipse.lsp4j.jsonrpc;
    
    //ResolutionException: Modules xercesImpl and jdk.xml.dom export package org.w3c.dom.html to module org.eclipse.lsp4xml
    requires java.xml;
    
    requires liquibase.core;
    requires com.h2database;
    requires java.sql;
    requires java.naming;
    
    requires java.logging;
    requires jabel;
    
    provides IXMLExtension with Plugin;
}
