package com.sifap.paymentprocessing.domain.calculation;

import java.math.BigDecimal;

/** Family factor — progressive scale by dependent count (BR-014). */
public final class FamilyFactor {

    private FamilyFactor() {}

    public static BigDecimal compute(int dependents) {
        if (dependents < 0) {
            throw new IllegalArgumentException("dependents must be ≥ 0");
        }
        if (dependents == 0)     return new BigDecimal("1.00");
        if (dependents <= 2)     return new BigDecimal("1.00").add(new BigDecimal("0.05").multiply(BigDecimal.valueOf(dependents)));
        if (dependents <= 4)     return new BigDecimal("1.10").add(new BigDecimal("0.03").multiply(BigDecimal.valueOf(dependents - 2)));
        return new BigDecimal("1.16").add(new BigDecimal("0.02").multiply(BigDecimal.valueOf(dependents - 4)));
    }
}
