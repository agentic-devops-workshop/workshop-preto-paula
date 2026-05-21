package com.sifap.paymentprocessing.domain.calculation;

import java.math.BigDecimal;

/**
 * The infamous K-factor (BR-010, MYS-003).
 *
 * <p>{@code K = 1.00 + (adjustmentFactor × 0.347215)}.
 *
 * <p>The constant {@code 0.347215} has no documentation in the legacy
 * code; archaeology suggests it was reverse-engineered from a 1997
 * spreadsheet that no longer exists. Preserving it is non-negotiable
 * for FR-EQUIV-001. Any future change requires a SENARC ruling and an
 * ADR superseding ADR-006.
 */
public final class KFactor {

    /** Documented magic constant from CADPROG.NSN#L81-L82. */
    public static final BigDecimal MAGIC = new BigDecimal("0.347215");

    private KFactor() {}

    public static BigDecimal compute(BigDecimal adjustmentFactor) {
        return BigDecimal.ONE.add(adjustmentFactor.multiply(MAGIC));
    }
}
