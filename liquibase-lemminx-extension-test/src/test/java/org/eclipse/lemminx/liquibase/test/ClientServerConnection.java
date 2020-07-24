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
package org.eclipse.lemminx.liquibase.test;

import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

import org.eclipse.lemminx.XMLServerLauncher;
import org.eclipse.lsp4j.ClientCapabilities;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.InitializeParams;
import org.eclipse.lsp4j.MessageActionItem;
import org.eclipse.lsp4j.MessageParams;
import org.eclipse.lsp4j.PublishDiagnosticsParams;
import org.eclipse.lsp4j.ShowMessageRequestParams;
import org.eclipse.lsp4j.TextDocumentClientCapabilities;
import org.eclipse.lsp4j.WorkspaceClientCapabilities;
import org.eclipse.lsp4j.launch.LSPLauncher;
import org.eclipse.lsp4j.services.LanguageClient;
import org.eclipse.lsp4j.services.LanguageServer;

public class ClientServerConnection {

    private static final int DEFAULT_TIMEOUT = 5_000;

    private static interface IPublishOnlyLanguageClient extends LanguageClient {

        @Override
        default public void telemetryEvent(Object object) {
        }

        @Override
        default public CompletableFuture<MessageActionItem> showMessageRequest(ShowMessageRequestParams requestParams) {
            return null;
        }

        @Override
        default public void showMessage(MessageParams messageParams) {
        }

        @Override
        default public void logMessage(MessageParams message) {
        }
        
        public List<Diagnostic> getDiagnostics();
        
    }
    private static final class PublishOnlyLanguageClient implements IPublishOnlyLanguageClient {

        List<Diagnostic> diagnostics = null;

        @Override
        public void publishDiagnostics(PublishDiagnosticsParams diagnosticsParams) {
            this.diagnostics = diagnosticsParams.getDiagnostics();            
        }

        public List<Diagnostic> getDiagnostics() {
            return diagnostics;
        }
        
    }

    private Future<?> server;
    private Future<Void> clientFuture;
    private LanguageServer languageServer;
    private IPublishOnlyLanguageClient client;

    public ClientServerConnection() throws IOException {
        var serverInputStream = new PipedInputStream();
        var clientOutputStream = new PipedOutputStream(serverInputStream);
        var serverOutputStream = new PipedOutputStream();
        var clientInputStream = new PipedInputStream(serverOutputStream);
        server = XMLServerLauncher.launch(serverInputStream, serverOutputStream);
        client = new PublishOnlyLanguageClient();
        var clientLauncher = LSPLauncher.createClientLauncher(client, clientInputStream, clientOutputStream);
        clientFuture = clientLauncher.startListening();
        languageServer = clientLauncher.getRemoteProxy();
        var initParams = new InitializeParams();
        var resourceDirectory = Paths.get("src","test","resources");
        initParams.setRootUri(resourceDirectory.toUri().toString());
        initParams.setCapabilities(new ClientCapabilities(new WorkspaceClientCapabilities(), new TextDocumentClientCapabilities(), false));
        getLanguageServer().initialize(initParams);
    }

    public void stop() throws InterruptedException, ExecutionException {
        try {
            getLanguageServer().shutdown().get(30, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            server.cancel(false);
        }
        clientFuture.cancel(true);
    }

    public boolean isEventuallySatisfied(Predicate<Collection<Diagnostic>> predicate, int timeoutMilliseconds) {
        var predicateResult = false;
        var start = System.currentTimeMillis();
        do {
            var currentDiagnostics = client.getDiagnostics();
            predicateResult = client.getDiagnostics() != null && predicate.test(currentDiagnostics);
            if (!predicateResult) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                }
            }
        } while (!predicateResult && System.currentTimeMillis() - start < timeoutMilliseconds);
        return predicateResult;
    }

    public boolean isEventuallySatisfied(Predicate<Collection<Diagnostic>> predicate) {
        return isEventuallySatisfied(predicate, DEFAULT_TIMEOUT);
    }

    public boolean isEventuallyFreeOfDiagnostics() {
        return isEventuallySatisfied(Collection<Diagnostic>::isEmpty, DEFAULT_TIMEOUT);
    }

    public boolean doesEventuallyContainMessage(String diagnosticMessage) {
        return isEventuallySatisfied(diagnostics -> {
            return diagnostics.stream().map(Diagnostic::getMessage).anyMatch(message -> {
                return message.contains(diagnosticMessage);
            });
        }, DEFAULT_TIMEOUT);
    }

    public LanguageServer getLanguageServer() {
        return languageServer;
    }

}
