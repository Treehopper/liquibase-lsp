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
package org.eclipse.lemminx.liquibase.validation;

import java.util.List;

import eu.hohenegger.lemminx.extensions.contentmodel.participants.wrapper.ErrorCode;
import eu.hohenegger.lemminx.extensions.contentmodel.participants.wrapper.LMXSyntaxErrorCode;
import eu.hohenegger.lemminx.services.extensions.diagnostics.AbstractLSPErrorReporter;
import eu.hohenegger.lemminx.xerces.wrapper.LMXLocator;
import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.Range;
import org.eclipse.lemminx.dom.DOMDocument;

public class ErrorReporter extends AbstractLSPErrorReporter {

    private static final String LIQUIBASE_DIAGNOSTIC_SOURCE = "Liquibase";
    
    private List<Diagnostic> overriddenDiagnostics;

    public ErrorReporter(DOMDocument xmlDocument, List<Diagnostic> diagnostics) {
        super(LIQUIBASE_DIAGNOSTIC_SOURCE, xmlDocument, diagnostics);
        this.overriddenDiagnostics = diagnostics;
    }

    @Override
    protected Range toLSPRange(LMXLocator location, String key, Object[] arguments, DOMDocument document) {
        // try adjust positions for Liquibase error
        var errorCode = ErrorCode.get(key);
        if (errorCode != null) {
            var range = ErrorCode.toLSPRange(location, errorCode, arguments, document);
            if (range != null) {
                return range;
            }
        }
        var syntaxCode = LMXSyntaxErrorCode.get(key);
        if (syntaxCode != null) {
            var range = LMXSyntaxErrorCode.toLSPRange(location, syntaxCode, arguments, document);
            if (range != null) {
                return range;
            }
        }
        return null;
    }
    
    public List<Diagnostic> getDiagnostics() {
        return overriddenDiagnostics;
    }
}
