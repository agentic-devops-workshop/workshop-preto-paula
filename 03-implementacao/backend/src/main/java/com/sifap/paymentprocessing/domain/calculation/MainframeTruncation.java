package com.sifap.paymentprocessing.domain.calculation;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * Mainframe-equivalent truncation (BR-020, MYS-005).
 *
 * <p>The legacy Natural code rounded DOWN at every step, never half-up.
 * This is the only class in the monolith allowed to invoke
 * {@link RoundingMode#DOWN} on a {@link BigDecimal}. An ArchUnit rule
 * enforces this — see {@code PaymentProcessingArchitectureTest}.
 */
public final class MainframeTruncation {

    private MainframeTruncation() {}

    /** Truncate to {@code scale} decimals (always DOWN). */
    public static BigDecimal applyDown(BigDecimal value, int scale) {
        return value.setScale(scale, RoundingMode.DOWN);
    }
}
