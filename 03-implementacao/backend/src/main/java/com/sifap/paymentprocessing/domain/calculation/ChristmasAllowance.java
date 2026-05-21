package com.sifap.paymentprocessing.domain.calculation;

import com.sifap.paymentprocessing.domain.Money;

import java.math.BigDecimal;

/** Christmas allowance, type-A programs only (BR-019, MYS-004). 15% of base, truncated. */
public final class ChristmasAllowance {

    private static final BigDecimal RATE = new BigDecimal("0.15");

    private ChristmasAllowance() {}

    public static Money compute(boolean isTypeA, boolean isDecember, Money base) {
        if (!isTypeA || !isDecember) return Money.ZERO;
        return base.times(RATE);
    }
}
