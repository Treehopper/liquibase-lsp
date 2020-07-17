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

import static java.util.Collections.singletonList;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.eclipse.lemminx.liquibase.test.jupiter.LemminxExtension;
import org.eclipse.lemminx.liquibase.test.jupiter.XMLSource;
import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionParams;
import org.eclipse.lsp4j.DidChangeTextDocumentParams;
import org.eclipse.lsp4j.Position;
import org.eclipse.lsp4j.TextDocumentContentChangeEvent;
import org.eclipse.lsp4j.TextDocumentIdentifier;
import org.eclipse.lsp4j.TextDocumentItem;
import org.eclipse.lsp4j.VersionedTextDocumentIdentifier;
import org.eclipse.lsp4j.services.TextDocumentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Timeout;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LemminxExtension.class)
public class LiquibaseDiagnosticParticipantTest {
    
    @Test
    @DisplayName("Completion tests: Random ID")
    @Timeout(15000)
    @XMLSource("""
        <?xml version="1.1" encoding="UTF-8" standalone="no"?>
        <databaseChangeLog
            xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
            xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd">
          <changeSet author="authorName" id="">           <!-- NO ID -->
            <createTable tableName="foobar">
              <column name="efg" type="TEXT">
                <constraints nullable="true" primaryKey="false" unique="false"/>
              </column>
            </createTable>
          </changeSet>
        </databaseChangeLog>
            """)
    public void testCompleteId(TextDocumentItem textDocumentItem, TextDocumentService textDocumentService) throws Throwable {
        var idPosition = new Position(6, 37);
        var documentIdentifier = new TextDocumentIdentifier(textDocumentItem.getUri());
        var idCompletitionParameter = new CompletionParams(documentIdentifier, idPosition);
        var either = textDocumentService.completion(idCompletitionParameter).get();
        assertTrue(either.isRight());

        var completionItems = either.getRight().getItems();
        assertTrue(completionItems.stream().map(CompletionItem::getLabel).anyMatch("Generate random ID"::equals));
    }
    
    @Test
    @DisplayName("Diagnostic tests: Bad column type")
    @XMLSource("""
            <?xml version="1.1" encoding="UTF-8" standalone="no"?>
            <databaseChangeLog
                xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd">
              <changeSet author="authorName" id="d21a626d-505a-4b75-800d-c63567224f83">
                <createTable tableName="foobar">
                  <column name="efg" type="TEXT">
                    <constraints nullable="true" primaryKey="false" unique="false"/>
                  </column>
                </createTable>
              </changeSet>
            
              <changeSet id="5472a591-d479-46d8-9cc8-7ef4e7ece067" author="foobar">
                <createTable tableName="mytable">
                    <column name="aname" type="COLUMN_TYPE"></column>      <!-- BAD TYPE -->
                </createTable>
              </changeSet>
            </databaseChangeLog>
                """)
    public void testBadTableColumnType(TextDocumentItem textDocumentItem, ClientServerConnection connection) {
        assertTrue(connection.doesEventuallyContainMessage("Failed SQL: (50004) CREATE TABLE PUBLIC.mytable (aname COLUMN_TYPE)"));

        var didChange = new DidChangeTextDocumentParams();
        didChange.setTextDocument(new VersionedTextDocumentIdentifier(textDocumentItem.getUri(), 2));
        var newDocumentContent = textDocumentItem.getText().replace("COLUMN_TYPE", "BIGINT");
        didChange.setContentChanges(singletonList(new TextDocumentContentChangeEvent(newDocumentContent)));
        connection.getLanguageServer().getTextDocumentService().didChange(didChange);
        assertTrue(connection.isEventuallyFreeOfDiagnostics());
    }
    
    @Test
    @DisplayName("Predecessor Test")
    @XMLSource("""
            <?xml version="1.1" encoding="UTF-8" standalone="no"?>
            <databaseChangeLog
                xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
                xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
                xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
                xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd">
              <include file="baseline.xml" />

              <changeSet id="5472a591-d479-46d8-9cc8-7ef4e7ece067" author="max">
                <addColumn tableName="foobar">
                    <column type="INT" name="efg"></column>
                </addColumn>
              </changeSet>
            </databaseChangeLog>
                """)
    public void testPredecessor(ClientServerConnection connection) {
        assertTrue(connection.doesEventuallyContainMessage("Failed SQL: (42121) ALTER TABLE PUBLIC.foobar ADD efg INT"));
    }
    
    
    @Test
    @DisplayName("Duplicate changeSet")
    @XMLSource("""
        <?xml version="1.1" encoding="UTF-8" standalone="no"?>
        <databaseChangeLog
            xmlns="http://www.liquibase.org/xml/ns/dbchangelog"
            xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext"
            xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
            xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.7.xsd">
          <changeSet author="authorName" id="9e5ab003-89ab-4517-bd8d-6acafa03da4d">
            <createTable tableName="ab">
              <column name="efgh" type="INT">
                <constraints nullable="true" primaryKey="false" unique="false"/>
              </column>
            </createTable>
          </changeSet>
          <changeSet author="authorName" id="9e5ab003-89ab-4517-bd8d-6acafa03da4d">
            <createTable tableName="a">
              <column name="efg" type="INT">
                <constraints nullable="true" primaryKey="false" unique="false"/>
              </column>
            </createTable>
          </changeSet>
        
          <changeSet id="5472a591-d479-46d8-9cc8-7ef4e7ece067" author="foobar">
            <createTable tableName="mytable">
                <column name="aname" type="FOO"></column>
            </createTable>
          </changeSet>
        </databaseChangeLog>
                """)
    public void testDuplicateChangeset(ClientServerConnection connection) {
        assertTrue(connection.doesEventuallyContainMessage("Duplicate changeSet"));
    }
    
    
    @Test
    @DisplayName("Drop Column: Missing column")
    @XMLSource("""
        <?xml version="1.1" encoding="UTF-8" standalone="no" ?>
        <databaseChangeLog xmlns="http://www.liquibase.org/xml/ns/dbchangelog" xmlns:ext="http://www.liquibase.org/xml/ns/dbchangelog-ext" xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xsi:schemaLocation="http://www.liquibase.org/xml/ns/dbchangelog-ext http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-ext.xsd http://www.liquibase.org/xml/ns/dbchangelog http://www.liquibase.org/xml/ns/dbchangelog/dbchangelog-3.6.xsd">
        
            <changeSet author="authorName" id="d21a626d-505a-4b75-800d-c63567224f83">
                <createTable tableName="foobar">
                    <column name="efg" type="TEXT">
                        <constraints nullable="true" primaryKey="false" unique="false" />
                    </column>
                </createTable>
            </changeSet>
        
            <changeSet author="authorName" id="11c661ac-ff81-448b-b28d-e08561672055">
                <dropColumn tableName=""></dropColumn>
            </changeSet>
        </databaseChangeLog>
                """)
    public void testColumnNameIsRequired(ClientServerConnection connection) {
        assertTrue(connection.doesEventuallyContainMessage("columnName is required"));
    }
    
}
