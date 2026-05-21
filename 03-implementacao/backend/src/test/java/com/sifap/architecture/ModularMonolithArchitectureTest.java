package com.sifap.architecture;

import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchRule;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Constitution Principle II — Modular Monolith — and Principle V — Single
 * Source of Truth — enforced as gates in CI.
 */
@AnalyzeClasses(packages = "com.sifap", importOptions = ImportOption.DoNotIncludeTests.class)
class ModularMonolithArchitectureTest {

    /** Bounded contexts may only depend on a sibling context through its published {@code api} package. */
    @ArchTest
    static final ArchRule contexts_cross_only_via_api =
        noClasses()
            .that().resideInAPackage("..beneficiary..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..paymentprocessing.application..",
                "..paymentprocessing.domain..",
                "..paymentprocessing.infrastructure..",
                "..paymentprocessing.interfaces..");

    @ArchTest
    static final ArchRule payment_does_not_depend_on_beneficiary_internals =
        noClasses()
            .that().resideInAPackage("..paymentprocessing..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "..beneficiary.application..",
                "..beneficiary.infrastructure..",
                "..beneficiary.interfaces..");

    /** Domain classes must not import any persistence framework. */
    @ArchTest
    static final ArchRule domain_is_persistence_free =
        noClasses()
            .that().resideInAPackage("..domain.calculation..")
            .should().dependOnClassesThat().resideInAnyPackage(
                "jakarta.persistence..", "org.springframework..", "org.hibernate..");

    /** Controllers must not reach into repositories directly — go through application services. */
    @ArchTest
    static final ArchRule controllers_use_services =
        noClasses()
            .that().resideInAPackage("..interfaces..")
            .should().dependOnClassesThat().resideInAPackage("..persistence..");

    /** BR-020 / Constitution Principle V: {@code RoundingMode.DOWN} only in {@code MainframeTruncation}. */
    @ArchTest
    static final ArchRule rounding_mode_down_only_in_truncation =
        classes()
            .that().haveSimpleName("MainframeTruncation")
            .should().resideInAPackage("..paymentprocessing.domain.calculation..");
    // The companion grep CI gate verifies no other source uses RoundingMode.DOWN — see scripts/check.sh.
}
