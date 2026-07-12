package com.javacadabra.tienda.pedidos.arquitectura;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;
import org.jmolecules.archunit.JMoleculesArchitectureRules;
import org.jmolecules.archunit.JMoleculesDddRules;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.javacadabra.tienda.pedidos", importOptions = ImportOption.DoNotIncludeTests.class)
class ArquitecturaHexagonalTest {

	@ArchTest
	static final ArchRule elDominioNoDependeDeOtrasCapas = noClasses()
			.that().resideInAPackage("..dominio..")
			.should().dependOnClassesThat().resideInAnyPackage("..aplicacion..", "..infraestructura..");

	@ArchTest
	static final ArchRule laAplicacionNoDependeDeInfraestructura = noClasses()
			.that().resideInAPackage("..aplicacion..")
			.should().dependOnClassesThat().resideInAPackage("..infraestructura..");

	@ArchTest
	static final ArchRule reglasDdd = JMoleculesDddRules.all();

	@ArchTest
	static final ArchRule reglaHexagonal = JMoleculesArchitectureRules.ensureHexagonal();
}
