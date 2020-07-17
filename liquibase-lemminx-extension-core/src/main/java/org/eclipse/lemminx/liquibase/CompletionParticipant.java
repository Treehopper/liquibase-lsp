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

import org.eclipse.lemminx.liquibase.completion.LiquibaseModel;
import org.eclipse.lemminx.services.extensions.CompletionParticipantAdapter;
import org.eclipse.lemminx.services.extensions.ICompletionRequest;
import org.eclipse.lemminx.services.extensions.ICompletionResponse;

public class CompletionParticipant extends CompletionParticipantAdapter {

    @Override
    public void onAttributeValue(String valuePrefix, ICompletionRequest request, ICompletionResponse response)
            throws Exception {
        if (!isLiquibase(request.getXMLDocument())) {
            return;
        }
        LiquibaseModel.computeValueCompletionResponses(request, response, request.getXMLDocument());
    }
}
