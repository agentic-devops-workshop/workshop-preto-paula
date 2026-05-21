package com.sifap.beneficiary.domain;

import java.util.Set;

/**
 * Brazilian CPF validator with the legacy-backdoor escape hatch (ADR-008).
 *
 * <p>By default the validator runs strict Mod-11. When constructed with
 * {@code allowLegacyBackdoor = true} it additionally accepts:
 * <ul>
 *   <li>the 8 prefixes {@code 000, 001, 002, 010, 011, 099, 100, 999}
 *       (BR-028, MYS-007),</li>
 *   <li>all-equal-digit CPFs ({@code 00000000000} … {@code 99999999999})
 *       (BR-029, EGG-002).</li>
 * </ul>
 *
 * <p>Both backdoor modes are forbidden in production by
 * {@code CpfBackdoorBootGuard}. They exist only to support migrating
 * legacy rows whose stored CPFs were originally accepted by the
 * Natural code in 1997.
 */
public final class BrazilianCpfValidator {

    private static final Set<String> LEGACY_PREFIXES =
        Set.of("000", "001", "002", "010", "011", "099", "100", "999");

    private final boolean allowLegacyBackdoor;

    public BrazilianCpfValidator(boolean allowLegacyBackdoor) {
        this.allowLegacyBackdoor = allowLegacyBackdoor;
    }

    public CpfValidationStatus validate(Cpf cpf) {
        String d = cpf.digits();
        if (isMod11Valid(d)) return CpfValidationStatus.VALID;
        if (!allowLegacyBackdoor) {
            throw new IllegalArgumentException("Invalid CPF (Mod-11 failure)");
        }
        if (isAllEqual(d) || LEGACY_PREFIXES.contains(d.substring(0, 3))) {
            return CpfValidationStatus.LEGACY_BACKDOOR;
        }
        throw new IllegalArgumentException("Invalid CPF (Mod-11 failure, no backdoor match)");
    }

    static boolean isMod11Valid(String d) {
        if (isAllEqual(d)) return false;          // not Mod-11 valid even though numerically it computes
        int dv1 = computeDigit(d, 9, 10);
        int dv2 = computeDigit(d, 10, 11);
        return dv1 == Character.getNumericValue(d.charAt(9))
            && dv2 == Character.getNumericValue(d.charAt(10));
    }

    private static int computeDigit(String d, int length, int startWeight) {
        int sum = 0;
        for (int i = 0; i < length; i++) {
            sum += Character.getNumericValue(d.charAt(i)) * (startWeight - i);
        }
        int mod = sum % 11;
        return mod < 2 ? 0 : 11 - mod;
    }

    private static boolean isAllEqual(String d) {
        char first = d.charAt(0);
        for (int i = 1; i < d.length(); i++) {
            if (d.charAt(i) != first) return false;
        }
        return true;
    }
}
