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
package org.eclipse.lemminx.liquibase;

import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lemminx.services.extensions.ICompletionParticipant;
import org.eclipse.lemminx.services.extensions.IXMLExtension;
import org.eclipse.lemminx.services.extensions.XMLExtensionsRegistry;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;
import org.eclipse.lemminx.services.extensions.save.ISaveContext;
import org.h2.tools.Server;

public class Plugin implements IXMLExtension {

    private static final Logger LOGGER = Logger.getLogger(Plugin.class.getName());
    
    private ICompletionParticipant completionParticipant;
    private IDiagnosticsParticipant diagnosticsParticipant;

    private String h2Server;
    private Server tcpServer;

    @Override
    public void start(InitializeParams params, XMLExtensionsRegistry registry) {
        
        try {
            tcpServer = Server.createTcpServer();
            tcpServer.start();
            h2Server = "tcp://localhost:" + tcpServer.getPort();
            LOGGER.log(Level.INFO, "H2 Server started: " + h2Server);
            
            String rootUri = params.getRootUri();
            LOGGER.log(Level.INFO, "Root URI: " + rootUri);

            completionParticipant = new CompletionParticipant();
            diagnosticsParticipant = new DiagnosticsParticipant(rootUri);

            registry.registerDiagnosticsParticipant(diagnosticsParticipant);
        } catch (SQLException e) {
            throw new RuntimeException("failed to start h2", e);
        } finally {
            
        }
        registry.registerCompletionParticipant(completionParticipant);
    }

    @Override
    public void stop(XMLExtensionsRegistry registry) {
        LOGGER.log(Level.INFO, "Stopping H2: " + h2Server);
        tcpServer.shutdown();
        LOGGER.log(Level.INFO, "Stopping H2 succeeded");
        registry.unregisterDiagnosticsParticipant(diagnosticsParticipant);
        registry.unregisterCompletionParticipant(completionParticipant);
    }

    @Override
    public void doSave(ISaveContext context) {
        // intentionally left empty
    }
}
