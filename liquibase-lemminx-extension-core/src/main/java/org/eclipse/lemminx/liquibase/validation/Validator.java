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

import static liquibase.wrapper.StringResourceAccessor.ANY_XML_FILENAME;
import static org.eclipse.lsp4j.DiagnosticSeverity.Error;
import static org.eclipse.lsp4j.DiagnosticSeverity.Warning;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URI;
import java.nio.file.Paths;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.eclipse.lemminx.dom.DOMDocument;

import liquibase.wrapper.FailureResilientLiquibase;
import liquibase.wrapper.LiquibaseValidationMessage;
import liquibase.wrapper.OpenValidationFailedException;
import liquibase.wrapper.StringResourceAccessor;

import org.eclipse.lsp4j.Diagnostic;
import org.eclipse.lsp4j.jsonrpc.CancelChecker;
import org.h2.jdbcx.JdbcDataSource;
import org.xml.sax.SAXParseException;

import liquibase.changelog.ChangeLogParameters;
import liquibase.database.DatabaseFactory;
import liquibase.database.jvm.JdbcConnection;
import liquibase.exception.DatabaseException;
import liquibase.exception.LiquibaseException;
import liquibase.exception.LiquibaseParseException;
import liquibase.exception.ValidationFailedException;
import liquibase.parser.core.xml.XMLChangeLogSAXParser;
import liquibase.resource.CompositeResourceAccessor;
import liquibase.resource.FileSystemResourceAccessor;
import liquibase.resource.ResourceAccessor;

public class Validator {

    private static final String CONTEXTS = "";
    private static final String H2_USER = "SA";
    private static final String H2_JDBC_URL = "jdbc:h2:mem:test";
    private static final Logger LOGGER = Logger.getLogger(Validator.class.getName());
    
    public static void doDiagnostics(DOMDocument document, List<Diagnostic> diagnostics, String rootUri, CancelChecker monitor) {
        LOGGER.log(Level.INFO, "Starting Liquibase Diagnostics");
        
        var reporter = new ErrorReporter(document, diagnostics);
        
        var liquibaseReporter = new LiquibaseErrorReporter(document, reporter);

        FailureResilientLiquibase liquibase = null;
        Connection connection = null;
        try {
            List<ResourceAccessor> openers = new ArrayList<>();
            StringResourceAccessor thisDocumentResourceAccessor = new StringResourceAccessor(document.getText());
            openers.add(thisDocumentResourceAccessor);
            
            if (rootUri != null) {
                var rootPath = Paths.get(URI.create(rootUri)).toFile();
                LOGGER.log(Level.INFO, "Running baseline, with base folder: " + rootPath);
                openers.add(new FileSystemResourceAccessor(rootPath));
                //TODO: when adding the ClassLoaderResourceAccessor, files are found multiple times 
                // openers.add(new ClassLoaderResourceAccessor());
            }
            var resourceAccessor = new CompositeResourceAccessor(openers);
            var liquibaseParser = new XMLChangeLogSAXParser();
            var parameters = new ChangeLogParameters();
            var changeLog = liquibaseParser.parse(ANY_XML_FILENAME, parameters, resourceAccessor);
            
            var randomInt = new Random().nextInt(); // avoiding concurrent access to the same DB 
            LOGGER.log(Level.INFO, "Connecting to: " + H2_JDBC_URL + randomInt);
            
            connection = createConnection(H2_JDBC_URL + randomInt, H2_USER);
            var jdbcConnection = new JdbcConnection(connection);
            var database = DatabaseFactory.getInstance().findCorrectDatabaseImplementation(jdbcConnection);
            
            liquibase = new FailureResilientLiquibase(changeLog, resourceAccessor, database, liquibaseReporter);
            
            if (!liquibase.isSafeToRunUpdate()) {
                LOGGER.log(Level.WARNING, "Not safe to run Liquibase");
            }
            try {
                liquibase.validate();
            } catch (OpenValidationFailedException e) {
                var visitor = e.getValidatingVisitor();
                var validationProblems = visitor.getValidationErrors();
                validationProblems.getErrorMessages().stream().map(LiquibaseValidationMessage::new).forEach(message -> {
                        liquibaseReporter.report(message.getChangeSetId(), "Error: " + message.getTheMessage(), Error);
                    });
                validationProblems.getRequiredErrorMessages().stream().map(LiquibaseValidationMessage::new).forEach(message -> {
                    liquibaseReporter.report(message.getChangeSetId(), "Required Error: " + message.getTheMessage(), Error);
                });
                validationProblems.getUnsupportedErrorMessages().stream().map(LiquibaseValidationMessage::new).forEach(message -> {
                    liquibaseReporter.report(message.getChangeSetId(), "Unsupported Error: " + message.getTheMessage(), Error);
                });
                validationProblems.getWarningMessages().stream().map(LiquibaseValidationMessage::new).forEach(message -> {
                    liquibaseReporter.report(message.getChangeSetId(), "Warning: " + message.getTheMessage(), Warning);
                });

                visitor.getDuplicateChangeSets().forEach(changeSet -> {
                    liquibaseReporter.report(changeSet, "Duplicate changeSet: " + changeSet.getId(), Error);
                });
                visitor.getSetupExceptions().forEach(setupException -> {
                    liquibaseReporter.reportOnDocument("Setup exception: " + setupException.getLocalizedMessage(), Error);
                });
                visitor.getErrorPreconditions().forEach(errorPrecondition -> {
                    liquibaseReporter.reportOnDocument("Error Precondition: " + errorPrecondition.getPrecondition().getName(), Error);
                });
                visitor.getFailedPreconditions().forEach(failedPrecondition -> {
                    liquibaseReporter.reportOnDocument("Failed Precondition: " + failedPrecondition.getPrecondition().getName(), Error);
                });
                //TODO: preconditions, setupExceptions, ...
                
                return;
            }
                
            LOGGER.log(Level.INFO, "Running Liquibase");
            liquibase.update(CONTEXTS);
        } catch (LiquibaseParseException e) {
            if (e.getCause() instanceof SAXParseException cause) {
                LOGGER.log(Level.INFO, "XML Parsing Error", cause);
            } else {
                LOGGER.log(Level.SEVERE, "Liquibase Parsing Error", e);
                liquibaseReporter.reportOnDocument("Liquibase Parsing Error: " + e.getMessage(), Error);
            }
        } catch (ValidationFailedException e) {
            //do nothing (covered by previous validation)
        } catch (LiquibaseException e) {
            LOGGER.log(Level.SEVERE, "Unexpected Liquibase error", e);            
        } catch (SQLException e) {
            LOGGER.log(Level.SEVERE, "Database Connection Error", e);
        }  catch (Throwable e) {
            //TODO: Remove this, and handle internal errors differently. It has been added to make debugging easier
            LOGGER.log(Level.SEVERE, "Unknown Error", e);
            StringWriter sw = new StringWriter();
        	PrintWriter pw = new PrintWriter(sw);
        	e.printStackTrace(pw);

//        	StackWalker
//	            .getInstance(Set.of(RETAIN_CLASS_REFERENCE, SHOW_REFLECT_FRAMES))
//	            .forEach(pw::println);
        	
            liquibaseReporter.reportOnDocument("Liquibase Parsing Error: " + sw.toString(), Error);
        } finally {
            LOGGER.log(Level.INFO, "Finishing Liquibase Diagnostics");
            
            if (liquibase != null) {
                try {
                    liquibase.getDatabase().close();
                } catch (DatabaseException e) {
                    LOGGER.log(Level.SEVERE, "Database Error while closing database", e);
                }
            }
        }
    }

    private static Connection createConnection(String jdbcUrl, String user) throws SQLException {
        var dataSource = new JdbcDataSource();
        dataSource.setURL(jdbcUrl);
        dataSource.setUser(user);
        return dataSource.getConnection();
    }
    
}
