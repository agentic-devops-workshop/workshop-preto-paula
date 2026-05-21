package com.sifap.beneficiary.domain;

import java.time.LocalDate;
import java.time.Period;

/** Age category derived from birth date (FR-024 / age recompute job). */
public enum AgeCategory {
    Minor, Adult, Senior;

    public static AgeCategory of(LocalDate birthDate, LocalDate today) {
        int years = Period.between(birthDate, today).getYears();
        if (years < 18) return Minor;
        if (years >= 60) return Senior;
        return Adult;
    }
}
