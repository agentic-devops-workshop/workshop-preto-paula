package com.sifap.paymentprocessing.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Objects;

/**
 * BRL monetary value. Always 2 decimals, always rounded DOWN (BR-020, MYS-005).
 *
 * <p>The legacy mainframe truncated to two decimals on every monetary
 * calculation. Modernizing with HALF_UP would change every payment value and
 * break audit equivalence (FR-EQUIV-001). This class is the single legal
 * doorway to monetary rounding in the system. An ArchUnit rule forbids
 * {@link RoundingMode#DOWN} elsewhere.
 */
public final class Money {

    public static final Money ZERO = new Money(BigDecimal.ZERO.setScale(2, RoundingMode.DOWN));

    private final BigDecimal value;

    private Money(BigDecimal value) {
        this.value = value;
    }

    public static Money of(BigDecimal raw) {
        Objects.requireNonNull(raw, "raw");
        return new Money(raw.setScale(2, RoundingMode.DOWN));
    }

    public static Money of(String raw) {
        return of(new BigDecimal(raw));
    }

    public Money plus(Money other) {
        return new Money(this.value.add(other.value).setScale(2, RoundingMode.DOWN));
    }

    public Money times(BigDecimal factor) {
        Objects.requireNonNull(factor, "factor");
        return new Money(this.value.multiply(factor).setScale(2, RoundingMode.DOWN));
    }

    public BigDecimal asBigDecimal() {
        return value;
    }

    @Override public boolean equals(Object o) {
        return o instanceof Money m && m.value.compareTo(this.value) == 0;
    }

    @Override public int hashCode() { return value.stripTrailingZeros().hashCode(); }

    @Override public String toString() { return value.toPlainString(); }
}
