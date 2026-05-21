package com.sifap.eligibility.domain;

import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityEquivalenceTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 6, 1);

    @ParameterizedTest
    @CsvFileSource(resources = "/fixtures/eligibility-legacy-fixture-2026-05.csv", numLinesToSkip = 1)
    void validate_should_match_legacy_fixture(String programCode,
                                              char type,
                                              String incomeMax,
                                              LocalDate birthDate,
                                              BigDecimal familyIncome,
                                              int dependents,
                                              short regionCode,
                                              String expectedOutcome,
                                              String expectedReason) {
        EligibilityCriteria criteria = new EligibilityCriteria(
            programCode,
            type,
            incomeMax == null || incomeMax.isBlank() ? null : new BigDecimal(incomeMax),
            true,
            false
        );

        EligibilityResult result = EligibilityValidator.validate(
            criteria, birthDate, familyIncome, dependents, regionCode, REFERENCE_DATE);

        String actualOutcome = switch (result) {
            case EligibilityResult.Eligible ignored -> "Eligible";
            case EligibilityResult.EligibleByBypass ignored -> "EligibleByBypass";
            case EligibilityResult.Ineligible ignored -> "Ineligible";
        };
        assertThat(actualOutcome).isEqualTo(expectedOutcome);
        if (result instanceof EligibilityResult.Ineligible ineligible) {
            assertThat(ineligible.reason().name()).isEqualTo(expectedReason);
        }
    }
}