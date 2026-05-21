package com.sifap.eligibility.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class EligibilityValidatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);

    private static EligibilityCriteria typeA(BigDecimal incomeMax) {
        return new EligibilityCriteria("BFA1", 'A', incomeMax, true, false);
    }

    private static EligibilityCriteria typeP() {
        return new EligibilityCriteria("BFP1", 'P', null, true, false);
    }

    private static EligibilityCriteria typeT() {
        return new EligibilityCriteria("BFT1", 'T', null, true, false);
    }

    // --- Region-99 bypass (BR-024 / MYS-008) ---

    @Test
    void region_99_short_circuits_to_bypass_regardless_of_anything_else() {
        EligibilityResult r = EligibilityValidator.validate(
            typeP(), LocalDate.of(2020, 1, 1), new BigDecimal("99999"),
            0, (short) 99, TODAY);
        assertThat(r).isInstanceOf(EligibilityResult.EligibleByBypass.class);
        assertThat(r.isBypass()).isTrue();
    }

    @Test
    void region_99_bypass_wins_over_invalid_input_only_when_inputs_are_not_null() {
        EligibilityResult r = EligibilityValidator.validate(
            null, LocalDate.of(2020, 1, 1), new BigDecimal("-500"),
            0, (short) 99, TODAY);
        // Invalid input is checked before bypass; we should NOT silently bypass invalid data
        assertThat(r).isInstanceOf(EligibilityResult.Ineligible.class);
    }

    // --- Type A ---

    @Test
    void typeA_eligible_when_income_below_max() {
        EligibilityResult r = EligibilityValidator.validate(
            typeA(new BigDecimal("600")), LocalDate.of(1990, 1, 1),
            new BigDecimal("550"), 0, (short) 26, TODAY);
        assertThat(r).isInstanceOf(EligibilityResult.Eligible.class);
    }

    @Test
    void typeA_ineligible_when_income_above_max_and_no_dependents() {
        EligibilityResult r = EligibilityValidator.validate(
            typeA(new BigDecimal("600")), LocalDate.of(1990, 1, 1),
            new BigDecimal("900"), 0, (short) 26, TODAY);
        assertThat(r).isInstanceOf(EligibilityResult.Ineligible.class);
        assertThat(((EligibilityResult.Ineligible) r).reason())
            .isEqualTo(Reason.INCOME_AND_NO_DEPENDENTS);
    }

    @Test
    void typeA_eligible_when_high_income_but_has_dependents_BR025() {
        EligibilityResult r = EligibilityValidator.validate(
            typeA(new BigDecimal("600")), LocalDate.of(1990, 1, 1),
            new BigDecimal("900"), 1, (short) 26, TODAY);
        assertThat(r).isInstanceOf(EligibilityResult.Eligible.class);
    }

    // --- Type P ---

    @Test
    void typeP_eligible_at_age_60_exactly_boundary_BR026() {
        LocalDate born60ago = TODAY.minusYears(60);
        EligibilityResult r = EligibilityValidator.validate(
            typeP(), born60ago, BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(r).isInstanceOf(EligibilityResult.Eligible.class);
    }

    @Test
    void typeP_ineligible_below_60() {
        EligibilityResult r = EligibilityValidator.validate(
            typeP(), TODAY.minusYears(59), BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(r).isInstanceOf(EligibilityResult.Ineligible.class);
        assertThat(((EligibilityResult.Ineligible) r).reason()).isEqualTo(Reason.AGE_BELOW_60);
    }

    // --- Type T ---

    @Test
    void typeT_eligible_at_age_16_boundary_inclusive_BR027() {
        EligibilityResult r = EligibilityValidator.validate(
            typeT(), TODAY.minusYears(16), BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(r).isInstanceOf(EligibilityResult.Eligible.class);
    }

    @Test
    void typeT_eligible_at_age_65_boundary_inclusive_BR027() {
        EligibilityResult r = EligibilityValidator.validate(
            typeT(), TODAY.minusYears(65), BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(r).isInstanceOf(EligibilityResult.Eligible.class);
    }

    @Test
    void typeT_ineligible_below_16() {
        EligibilityResult r = EligibilityValidator.validate(
            typeT(), TODAY.minusYears(15), BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(((EligibilityResult.Ineligible) r).reason()).isEqualTo(Reason.AGE_BELOW_16);
    }

    @Test
    void typeT_ineligible_above_65() {
        EligibilityResult r = EligibilityValidator.validate(
            typeT(), TODAY.minusYears(66), BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(((EligibilityResult.Ineligible) r).reason()).isEqualTo(Reason.AGE_ABOVE_65);
    }

    // --- Edge cases ---

    @Test
    void program_not_found_returns_structured_result_not_exception() {
        EligibilityResult r = EligibilityValidator.validate(
            null, LocalDate.of(1990, 1, 1), BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(((EligibilityResult.Ineligible) r).reason()).isEqualTo(Reason.PROGRAM_NOT_FOUND);
    }

    @Test
    void retired_program_returns_program_retired() {
        EligibilityCriteria retired = new EligibilityCriteria("BFA1", 'A', new BigDecimal("600"), false, true);
        EligibilityResult r = EligibilityValidator.validate(
            retired, LocalDate.of(1990, 1, 1), BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(((EligibilityResult.Ineligible) r).reason()).isEqualTo(Reason.PROGRAM_RETIRED);
    }

    @Test
    void negative_income_returns_invalid_input_not_exception() {
        EligibilityResult r = EligibilityValidator.validate(
            typeA(new BigDecimal("600")), LocalDate.of(1990, 1, 1),
            new BigDecimal("-100"), 0, (short) 26, TODAY);
        assertThat(((EligibilityResult.Ineligible) r).reason()).isEqualTo(Reason.INVALID_INPUT);
    }

    @Test
    void negative_dependents_returns_invalid_input_not_exception() {
        EligibilityResult r = EligibilityValidator.validate(
            typeA(new BigDecimal("600")), LocalDate.of(1990, 1, 1),
            BigDecimal.ZERO, -1, (short) 26, TODAY);
        assertThat(((EligibilityResult.Ineligible) r).reason()).isEqualTo(Reason.INVALID_INPUT);
    }

    @Test
    void future_birth_date_returns_invalid_input_not_exception() {
        EligibilityResult r = EligibilityValidator.validate(
            typeA(new BigDecimal("600")), TODAY.plusDays(1),
            BigDecimal.ZERO, 0, (short) 26, TODAY);
        assertThat(((EligibilityResult.Ineligible) r).reason()).isEqualTo(Reason.INVALID_INPUT);
    }

    @ParameterizedTest
    @MethodSource("invalidRegions")
    void unknown_region_returns_invalid_input_before_program_lookup(short regionCode) {
        EligibilityResult r = EligibilityValidator.validate(
            null, LocalDate.of(1990, 1, 1), BigDecimal.ZERO, 0, regionCode, TODAY);

        assertThat(r).isInstanceOf(EligibilityResult.Ineligible.class);
        EligibilityResult.Ineligible ineligible = (EligibilityResult.Ineligible) r;
        assertThat(ineligible.reason()).isEqualTo(Reason.INVALID_INPUT);
        assertThat(ineligible.detail()).contains("regionCode=" + regionCode);
    }

    private static Stream<Arguments> invalidRegions() {
        return Stream.of(
            Arguments.of((short) 0),
            Arguments.of((short) 27),
            Arguments.of((short) 50),
            Arguments.of((short) 127)
        );
    }
}
