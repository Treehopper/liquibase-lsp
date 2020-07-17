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
package org.eclipse.lemminx.liquibase.test.jupiter;

import java.io.IOException;
import java.net.URISyntaxException;

import org.eclipse.lemminx.liquibase.test.ClientServerConnection;
import org.eclipse.lsp4j.DidOpenTextDocumentParams;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.junit.jupiter.api.extension.AfterEachCallback;
import org.junit.jupiter.api.extension.BeforeEachCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.ParameterContext;
import org.junit.jupiter.api.extension.ParameterResolutionException;
import org.junit.jupiter.api.extension.ParameterResolver;

public class LemminxExtension implements ParameterResolver, BeforeEachCallback, AfterEachCallback {
    
    private ClientServerConnection connection;

    @Override
    public boolean supportsParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        var parameterType = parameterContext.getParameter().getType();
        if (parameterType.isAssignableFrom(TextDocumentItem.class)) {
            var executable = parameterContext.getDeclaringExecutable();
            if (executable.isAnnotationPresent(XMLSource.class)) {
                return true;
            }
        } else if (parameterType.isAssignableFrom(TextDocumentService.class)) {
            return true;
        } else if (parameterType.isAssignableFrom(ClientServerConnection.class)) {
            return true;
        }
        return false;
    }

    @Override
    public Object resolveParameter(ParameterContext parameterContext, ExtensionContext extensionContext)
            throws ParameterResolutionException {
        TextDocumentItem textDocumentItem = null;
        var executable = parameterContext.getDeclaringExecutable();
        if (executable.isAnnotationPresent(XMLSource.class)) {
            XMLSource annotation = executable.getAnnotation(XMLSource.class);
            String string = annotation.value().trim();
            try {
                textDocumentItem = createTextDocumentItem(string);
            } catch (IOException | URISyntaxException e) {
                throw new ParameterResolutionException("Unable to resolve XML parameter", e);
            }
        }
        
        var parameterType = parameterContext.getParameter().getType();
        if (parameterType.isAssignableFrom(TextDocumentItem.class)) {
            if (executable.isAnnotationPresent(XMLSource.class)) {
                return textDocumentItem;
            }
        } else if (parameterType.isAssignableFrom(TextDocumentService.class)) {
            return connection.getLanguageServer().getTextDocumentService();
        } else if (parameterType.isAssignableFrom(ClientServerConnection.class)) {
            return connection;
        }

        return null;
    }
    
    public TextDocumentItem createTextDocumentItem(String contents) throws IOException, URISyntaxException {
        var textDocumentItem = new TextDocumentItem("mem:/junit-file.xml", "xml", 1, contents);
        var params = new DidOpenTextDocumentParams(textDocumentItem);
        connection.getLanguageServer().getTextDocumentService().didOpen(params);
        return textDocumentItem;
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        connection.stop();
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        connection = new ClientServerConnection();
    }

}
