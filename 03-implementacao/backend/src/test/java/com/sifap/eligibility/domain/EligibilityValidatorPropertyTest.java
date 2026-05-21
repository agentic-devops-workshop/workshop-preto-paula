package com.sifap.eligibility.domain;

import net.jqwik.api.Arbitraries;
import net.jqwik.api.Arbitrary;
import net.jqwik.api.ForAll;
import net.jqwik.api.Property;
import net.jqwik.api.Provide;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityValidatorPropertyTest {

    private static final LocalDate REFERENCE_DATE = LocalDate.of(2026, 6, 1);
    private static final EligibilityCriteria TYPE_A = new EligibilityCriteria(
        "BFA1", 'A', new BigDecimal("600"), true, false);

    @Property
    void validate_should_return_bypass_when_region_is_99(
        @ForAll("birthDates") LocalDate birthDate,
        @ForAll("nonNegativeMoney") BigDecimal familyIncome,
        @ForAll("dependents") int dependents
    ) {
        EligibilityResult result = EligibilityValidator.validate(
            TYPE_A, birthDate, familyIncome, dependents, (short) 99, REFERENCE_DATE);

        assertThat(result.isBypass()).isTrue();
    }

    @Property
    void validate_should_only_reject_type_a_when_income_is_above_limit_and_dependents_are_zero(
        @ForAll("incomeAboveLimit") BigDecimal familyIncome
    ) {
        EligibilityResult result = EligibilityValidator.validate(
            TYPE_A, LocalDate.of(1990, 1, 1), familyIncome, 0, (short) 26, REFERENCE_DATE);

        assertThat(result).isInstanceOf(EligibilityResult.Ineligible.class);
        assertThat(((EligibilityResult.Ineligible) result).reason()).isEqualTo(Reason.INCOME_AND_NO_DEPENDENTS);
    }

    @Provide
    Arbitrary<LocalDate> birthDates() {
        return Arbitraries.integers()
            .between(0, 46_000)
            .map(REFERENCE_DATE::minusDays);
    }

    @Provide
    Arbitrary<BigDecimal> nonNegativeMoney() {
        return Arbitraries.bigDecimals().between(BigDecimal.ZERO, new BigDecimal("10000"));
    }

    @Provide
    Arbitrary<BigDecimal> incomeAboveLimit() {
        return Arbitraries.bigDecimals().between(new BigDecimal("600.01"), new BigDecimal("10000"));
    }

    @Provide
    Arbitrary<Integer> dependents() {
        return Arbitraries.integers().between(0, 20);
    }
}