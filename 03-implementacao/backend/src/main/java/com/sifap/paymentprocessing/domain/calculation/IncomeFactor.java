package com.sifap.paymentprocessing.domain.calculation;

import java.math.BigDecimal;

/** Income factor — inverse 5-bracket table (BR-015). */
public final class IncomeFactor {

    private IncomeFactor() {}

    public static BigDecimal compute(BigDecimal familyIncome) {
        if (familyIncome.signum() < 0) throw new IllegalArgumentException("income must be ≥ 0");
        int cmp300  = familyIncome.compareTo(new BigDecimal("300"));
        int cmp600  = familyIncome.compareTo(new BigDecimal("600"));
        int cmp900  = familyIncome.compareTo(new BigDecimal("900"));
        int cmp1500 = familyIncome.compareTo(new BigDecimal("1500"));
        if (cmp300 <= 0)  return new BigDecimal("1.00");
        if (cmp600 <= 0)  return new BigDecimal("0.85");
        if (cmp900 <= 0)  return new BigDecimal("0.70");
        if (cmp1500 <= 0) return new BigDecimal("0.55");
        return new BigDecimal("0.40");
    }
}
