package com.sifap.paymentprocessing.domain.calculation;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.Period;

/** Age factor (BR-016). Senior {@code ≥65}: +15%. Senior {@code ≥60}: +10%. Minor {@code <18}: +5%. */
public final class AgeFactor {

    private AgeFactor() {}

    public static BigDecimal compute(LocalDate birthDate, LocalDate referenceDate) {
        int years = Period.between(birthDate, referenceDate).getYears();
        if (years >= 65) return new BigDecimal("1.15");
        if (years >= 60) return new BigDecimal("1.10");
        if (years < 18)  return new BigDecimal("1.05");
        return BigDecimal.ONE;
    }
}
