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

import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.dom.DOMNode;
import org.eclipse.lsp4j.DiagnosticSeverity;

import liquibase.changelog.ChangeSet;
import liquibase.wrapper.ILiquibaseErrorReporter;

public class LiquibaseErrorReporter implements ILiquibaseErrorReporter {
    
    private static final String CHANGE_SET_ID_ATTRIBUTE = "id";

    private static final Logger LOGGER = Logger.getLogger(LiquibaseErrorReporter.class.getName());

    private ErrorReporter reporter;
    private DOMDocument document;

    public LiquibaseErrorReporter(DOMDocument document, ErrorReporter reporter) {
        this.document = document;
        this.reporter = reporter;
    }

    public void report(ChangeSet changeSet, String message, DiagnosticSeverity severity) {
        var roots = document.getRoots();
        for (var root : roots) {
            report(changeSet.getId(), message, root, severity);
        }
    }

    public void report(String changeSetId, String message, DiagnosticSeverity severity) {
        var roots = document.getRoots();
        for (var root : roots) {
            report(changeSetId, message, root, severity);
        }
    }

    private void report(String changeSetId, String message, DOMNode root, DiagnosticSeverity severity) {
        var children = root.getChildrenWithAttributeValue(CHANGE_SET_ID_ATTRIBUTE, changeSetId);
        for (var node : children) {
            report(message, severity, node);
        }
    }
    
    public void reportOnDocument(String message, DiagnosticSeverity severity) {
        var adjustedRange = document.getTrimmedRange(document.getStart(), document.getEnd());
        LOGGER.log(Level.INFO, message + " at " + adjustedRange);
        reporter.addDiagnostic(adjustedRange, message, severity, "MigrationFailedException");
    }

    public void report(String message, DiagnosticSeverity severity, DOMNode node) {
        var adjustedRange = document.getTrimmedRange(node.getStart(), node.getEnd());
        LOGGER.log(Level.INFO, message + " at " + adjustedRange);
        reporter.addDiagnostic(adjustedRange, message, severity, "MigrationFailedException");
    }

}
