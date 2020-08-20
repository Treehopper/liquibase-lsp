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
package org.eclipse.lemminx.liquibase.test.archunit;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.library.plantuml.PlantUmlArchCondition.adhereToPlantUmlDiagram;
import static com.tngtech.archunit.library.plantuml.PlantUmlArchCondition.Configurations.consideringOnlyDependenciesInDiagram;

import java.net.URL;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;

@DisplayName("TODO")
public class ComponentCheckArchUnitTest {

	private URL myDiagram;
	private JavaClasses classes;

	@BeforeEach
	public void setUp() {
		classes = new ClassFileImporter().withImportOption(location -> {
			return !location.contains("/test/");
		}).importClasspath();
		myDiagram = getClass().getResource("/architecture.puml");

	}

	@Test
	@DisplayName("TODO")
	public void testUml() {
		classes().should(adhereToPlantUmlDiagram(myDiagram, consideringOnlyDependenciesInDiagram())).check(classes);
	}

}
