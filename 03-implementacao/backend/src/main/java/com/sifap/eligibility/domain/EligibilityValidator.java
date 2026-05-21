package com.sifap.eligibility.domain;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/**
 * The heart of feature 004. Pure function — no I/O, no state, no events.
 *
 * <p>Order of evaluation matters:
 * <ol>
 *   <li>Reject invalid inputs (FR-010).</li>
 *   <li>Region-99 short-circuit (FR-005, BR-024, MYS-008) — wins over everything else.</li>
 *   <li>Program presence + Retired status (FR-001 edge cases).</li>
 *   <li>Type-specific rule (FR-002/003/004).</li>
 * </ol>
 */
public final class EligibilityValidator {

    private static final short REGION_BYPASS = 99;

    private EligibilityValidator() {}

    public static EligibilityResult validate(
        EligibilityCriteria criteria,
        LocalDate birthDate,
        BigDecimal familyIncome,
        int dependents,
        short regionCode,
        LocalDate referenceDate
    ) {
        if (birthDate == null || familyIncome == null || referenceDate == null) {
            return new EligibilityResult.Ineligible(Reason.INVALID_INPUT, "null required field");
        }
        if (familyIncome.signum() < 0 || dependents < 0) {
            return new EligibilityResult.Ineligible(Reason.INVALID_INPUT, "negative income or dependents");
        }

        // BR-024 / MYS-008 — region 99 wins. Documented bypass with audit on the calling side (FR-006).
        if (regionCode == REGION_BYPASS) {
            return new EligibilityResult.EligibleByBypass(REGION_BYPASS);
        }

        if (criteria == null) {
            return new EligibilityResult.Ineligible(Reason.PROGRAM_NOT_FOUND, null);
        }
        if (criteria.retired()) {
            return new EligibilityResult.Ineligible(Reason.PROGRAM_RETIRED, criteria.programCode());
        }

        int age = Period.between(birthDate, referenceDate).getYears();
        return switch (criteria.type()) {
            case 'A' -> evaluateTypeA(criteria, familyIncome, dependents);
            case 'P' -> evaluateTypeP(age);
            case 'T' -> evaluateTypeT(age);
            default  -> new EligibilityResult.Ineligible(Reason.INVALID_INPUT, "unknown type " + criteria.type());
        };
    }

    /** BR-025: type A. Eligible IFF income ≤ max OR dependents ≥ 1. */
    private static EligibilityResult evaluateTypeA(EligibilityCriteria c, BigDecimal income, int deps) {
        boolean incomeOk = c.incomeMax() != null && income.compareTo(c.incomeMax()) <= 0;
        if (incomeOk || deps >= 1) return new EligibilityResult.Eligible();
        return new EligibilityResult.Ineligible(
            Reason.INCOME_AND_NO_DEPENDENTS,
            "income=%s max=%s dependents=%d".formatted(income, c.incomeMax(), deps));
    }

    /** BR-026: type P (idoso). Eligible IFF age ≥ 60. */
    private static EligibilityResult evaluateTypeP(int age) {
        return age >= 60
            ? new EligibilityResult.Eligible()
            : new EligibilityResult.Ineligible(Reason.AGE_BELOW_60, "age=" + age);
    }

    /** BR-027: type T (trabalhador). Eligible IFF 16 ≤ age ≤ 65. */
    private static EligibilityResult evaluateTypeT(int age) {
        if (age < 16) return new EligibilityResult.Ineligible(Reason.AGE_BELOW_16, "age=" + age);
        if (age > 65) return new EligibilityResult.Ineligible(Reason.AGE_ABOVE_65, "age=" + age);
        return new EligibilityResult.Eligible();
    }
}
