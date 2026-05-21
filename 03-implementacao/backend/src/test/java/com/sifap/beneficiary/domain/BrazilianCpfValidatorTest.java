package com.sifap.beneficiary.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrazilianCpfValidatorTest {

    private final BrazilianCpfValidator strict   = new BrazilianCpfValidator(false);
    private final BrazilianCpfValidator backdoor = new BrazilianCpfValidator(true);

    @Test
    void accepts_valid_mod11_cpf() {
        // Known-valid CPF: 11144477735
        assertThat(strict.validate(Cpf.parse("11144477735")))
            .isEqualTo(CpfValidationStatus.VALID);
    }

    @Test
    void rejects_invalid_mod11_in_strict_mode() {
        assertThatThrownBy(() -> strict.validate(Cpf.parse("12345678901")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void rejects_all_equal_digits_in_strict_mode_BR029() {
        assertThatThrownBy(() -> strict.validate(Cpf.parse("00000000000")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void accepts_all_equal_digits_via_backdoor_for_legacy_rows() {
        assertThat(backdoor.validate(Cpf.parse("00000000000")))
            .isEqualTo(CpfValidationStatus.LEGACY_BACKDOOR);
    }

    @Test
    void accepts_legacy_prefix_999_via_backdoor_BR028() {
        // 99988877766 — invalid Mod-11, prefix 999 → backdoor accepts
        assertThat(backdoor.validate(Cpf.parse("99988877766")))
            .isEqualTo(CpfValidationStatus.LEGACY_BACKDOOR);
    }

    @Test
    void backdoor_does_not_accept_arbitrary_invalid_cpf() {
        assertThatThrownBy(() -> backdoor.validate(Cpf.parse("12345678901")))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void masker_keeps_last_two_digits() {
        assertThat(Cpf.parse("11144477735").masked()).isEqualTo("XXX.XXX.XXX-35");
    }
}
