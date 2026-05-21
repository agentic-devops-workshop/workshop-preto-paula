package com.sifap.beneficiary.interfaces.dto;

import com.sifap.beneficiary.domain.AgeCategory;
import com.sifap.beneficiary.domain.Beneficiary;
import com.sifap.beneficiary.domain.BrazilianCpfMasker;
import com.sifap.beneficiary.domain.CpfValidationStatus;
import com.sifap.beneficiary.domain.LifecycleStatus;

import java.time.LocalDate;

/**
 * Outgoing representation of a {@link Beneficiary}.
 *
 * <p>CPF is always masked here per ADR-007. The full value is exposed
 * only through {@code POST /beneficiaries/{cpf}/reveal-cpf}.
 */
public record BeneficiaryDto(
    Long id,
    String cpf,
    String nis,
    String name,
    LocalDate birthDate,
    LifecycleStatus lifecycleStatus,
    AgeCategory ageCategory,
    CpfValidationStatus cpfValidationStatus,
    String programCode
) {
    public static BeneficiaryDto from(Beneficiary b) {
        return new BeneficiaryDto(
            b.getId(),
            BrazilianCpfMasker.mask(b.getCpf()),
            b.getNis(),
            b.getName(),
            b.getBirthDate(),
            b.getLifecycleStatus(),
            b.getAgeCategory(),
            b.getCpfValidationStatus(),
            b.getProgramCode()
        );
    }
}
