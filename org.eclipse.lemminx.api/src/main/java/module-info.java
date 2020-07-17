/*******************************************************************************
 * Copyright (C) 2020, Max Hohenegger <eclipse@hohenegger.eu>
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License 2.0
 * which accompanies this distribution, and is available at
 * https://www.eclipse.org/legal/epl-2.0/
 *
 * SPDX-License-Identifier: EPL-2.0
 *******************************************************************************/
module lemminx.api {
    exports eu.hohenegger.lemminx.services.extensions.diagnostics;
    exports eu.hohenegger.lemminx.xerces.wrapper;
    exports eu.hohenegger.lemminx.extensions.contentmodel.participants.wrapper;
    exports eu.hohenegger.lemminx.utils.wrapper;
    
    requires org.eclipse.lemminx;
    requires org.eclipse.lsp4j;
    requires xercesImpl;
    requires java.logging;
}
