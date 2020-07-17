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

import org.eclipse.lemminx.dom.DOMDocument;

public class IsLiquibase {

    private static final String LIQUIBASE_EXTENSION = ".xml";
    private static final String LIQUIBASE_SCHEMA_NS = "http://www.liquibase.org/xml/ns/dbchangelog";

    private IsLiquibase() {
        //intentionally left blank
    }

    public static boolean isLiquibase(DOMDocument document) {
        if (document == null) {
            return false;
        }
        var uriString = document.getDocumentURI();
        if (isLiquibase(uriString)) {
            return true;
        }
        return checkRootNamespace(document, LIQUIBASE_SCHEMA_NS);
    }

    private static boolean isLiquibase(String uri) {
        return uri != null && uri.endsWith(LIQUIBASE_EXTENSION);
    }

    private static boolean checkRootNamespace(DOMDocument document, String namespace) {
        var documentElement = document.getDocumentElement();
        return documentElement != null && namespace.equals(documentElement.getNamespaceURI());
    }

}

