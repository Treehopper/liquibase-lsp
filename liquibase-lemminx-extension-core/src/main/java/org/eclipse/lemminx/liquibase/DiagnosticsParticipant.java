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

import static org.eclipse.lemminx.liquibase.IsLiquibase.isLiquibase;

import java.util.List;

import org.eclipse.lemminx.liquibase.validation.Validator;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.diagnostics.IDiagnosticsParticipant;


public class DiagnosticsParticipant implements IDiagnosticsParticipant {

    private String rootUri;

    public DiagnosticsParticipant(String rootUri) {
        this.rootUri = rootUri;
    }

    @Override
    public void doDiagnostics(DOMDocument document, List<Diagnostic> diagnostics, CancelChecker monitor) {
        if (!isLiquibase(document)) {
            return;
        }
        
        Validator.doDiagnostics(document, diagnostics, rootUri, monitor);
    }


}

