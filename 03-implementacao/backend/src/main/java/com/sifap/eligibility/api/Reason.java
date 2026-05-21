package com.sifap.eligibility.api;

/** Stable public ineligibility reason codes for callers, logs, and UI. */
public enum Reason {
    INVALID_INPUT,
    PROGRAM_NOT_FOUND,
    PROGRAM_RETIRED,
    INCOME_AND_NO_DEPENDENTS,
    AGE_BELOW_60,
    AGE_BELOW_16,
    AGE_ABOVE_65
}