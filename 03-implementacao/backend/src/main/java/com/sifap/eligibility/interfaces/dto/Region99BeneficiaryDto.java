package com.sifap.eligibility.interfaces.dto;

import java.time.LocalDate;

public record Region99BeneficiaryDto(String cpf, String maskedCpf, int ageAtRun, LocalDate lastUpdate) {

    public static Region99BeneficiaryDto from(String cpf, int ageAtRun, LocalDate lastUpdate, boolean revealCpf) {
        return new Region99BeneficiaryDto(
            revealCpf ? cpf : null,
            revealCpf ? cpf : maskCpf(cpf),
            ageAtRun,
            lastUpdate
        );
    }

    private static String maskCpf(String cpf) {
        if (cpf == null || cpf.length() < 2) return "XXX.XXX.XXX-NN";
        String digits = cpf.replaceAll("\\D", "");
        if (digits.length() != 11) return "XXX.XXX.XXX-NN";
        return "XXX.XXX.XXX-" + digits.substring(9);
    }
}