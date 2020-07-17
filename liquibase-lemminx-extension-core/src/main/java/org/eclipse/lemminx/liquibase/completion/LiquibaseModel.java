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
package org.eclipse.lemminx.liquibase.completion;

import java.util.UUID;
import java.util.logging.Logger;

import org.eclipse.lsp4j.CompletionItem;
import org.eclipse.lsp4j.CompletionItemKind;
import org.eclipse.lsp4j.TextEdit;
import org.eclipse.lemminx.commons.BadLocationException;
import org.eclipse.lemminx.dom.DOMDocument;
import org.eclipse.lemminx.services.extensions.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.ICompletionResponse;

public class LiquibaseModel {
    
    private static final Logger LOGGER = Logger.getLogger(LiquibaseModel.class.getName());

    private static final String ID_ATTRIBUTE = "id";
    private static final String CHANGE_SET_TAG = "changeSet";

    public static void computeValueCompletionResponses(ICompletionRequest request, ICompletionResponse response,
            DOMDocument document) throws BadLocationException {
        var editRange = request.getReplaceRange();
        int offset = document.offsetAt(editRange.getStart());

        var node = document.findNodeAt(offset);
        if(!CHANGE_SET_TAG.equals(node.getNodeName())) {
            return;
        }

        var attr = node.findAttrAt(offset);
        if(!ID_ATTRIBUTE.equals(attr.getName())) {
            return;
        }
        
        LOGGER.info("Completion at " + editRange);
        
        CompletionItem item;
        item = new CompletionItem();
        var insertText = request.getInsertAttrValue(UUID.randomUUID().toString());
        item.setLabel("Generate random ID");
        item.setFilterText(insertText);
        item.setKind(CompletionItemKind.Enum);
        item.setTextEdit(new TextEdit(editRange, insertText));
        response.addCompletionItem(item);
    }

}
