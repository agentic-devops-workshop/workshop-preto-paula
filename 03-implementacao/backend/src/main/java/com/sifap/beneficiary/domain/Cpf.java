package com.sifap.beneficiary.domain;

import java.util.Objects;

/**
 * Brazilian Cadastro de Pessoas Físicas (CPF) — 11 digits, no formatting.
 *
 * <p>Validation strictness is delegated to {@link BrazilianCpfValidator}.
 * This class enforces only the structural rule of 11 digits.
 */
public final class Cpf {

    private final String digits;

    private Cpf(String digits) { this.digits = digits; }

    /** Parse, stripping any non-digit. Throws when the result is not 11 digits. */
    public static Cpf parse(String raw) {
        Objects.requireNonNull(raw, "raw");
        String digits = raw.replaceAll("\\D", "");
        if (digits.length() != 11) {
            throw new IllegalArgumentException("CPF must have 11 digits, got " + digits.length());
        }
        return new Cpf(digits);
    }

    public String digits() { return digits; }

    public String masked() { return BrazilianCpfMasker.mask(this); }

    @Override public boolean equals(Object o) { return o instanceof Cpf c && c.digits.equals(this.digits); }
    @Override public int hashCode() { return digits.hashCode(); }

    /** {@inheritDoc} Defaults to the masked form to keep raw CPF out of logs. */
    @Override public String toString() { return masked(); }
}
