package com.sifap.beneficiary.domain;

/**
 * CPF masking policy per ADR-007.
 *
 * <p>Output format: {@code XXX.XXX.XXX-NN} where {@code NN} are the last
 * two digits of the original CPF. This is the only place in the system
 * authorized to render a CPF for display, log, CSV, or response body.
 */
public final class BrazilianCpfMasker {

    private BrazilianCpfMasker() {}

    public static String mask(Cpf cpf) { return mask(cpf.digits()); }

    public static String mask(String digits) {
        if (digits == null || digits.length() != 11) {
            return "XXX.XXX.XXX-XX";
        }
        return "XXX.XXX.XXX-" + digits.substring(9);
    }
}
