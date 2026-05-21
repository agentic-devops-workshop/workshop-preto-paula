package com.sifap.eligibility.domain;

/** Documented ineligibility reasons (FR-011). Stable text for logs and UI. */
public enum Reason {
    INCOME_AND_NO_DEPENDENTS,
    AGE_BELOW_60,
    AGE_BELOW_16,
    AGE_ABOVE_65,
    PROGRAM_NOT_FOUND,
    PROGRAM_RETIRED,
    INVALID_INPUT
}
