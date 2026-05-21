package com.sifap.eligibility.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

@AnalyzeClasses(packages = "com.sifap", importOptions = ImportOption.DoNotIncludeTests.class)
class EligibilityArchitectureTest {

    @ArchTest
    static final ArchRule other_modules_may_only_import_eligibility_api =
        noClasses()
            .that().resideOutsideOfPackage("..eligibility..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..eligibility.domain..",
                "..eligibility.application..",
                "..eligibility.infrastructure..",
                "..eligibility.interfaces..");

    @ArchTest
    static final ArchRule eligibility_domain_is_framework_free =
        noClasses()
            .that().resideInAPackage("..eligibility.domain..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta.persistence..",
                "org.springframework..",
                "org.hibernate..");
}