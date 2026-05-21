package com.sifap.beneficiary.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record RegisterBeneficiaryRequest(
    @NotBlank @Pattern(regexp = "\\d{11}") String cpf,
    @NotBlank @Size(min = 3, max = 120)    String name,
    @NotNull  @Past                        LocalDate birthDate,
    @NotBlank @Pattern(regexp = "[MF]")    String sex,
    @NotBlank @Size(min = 4, max = 4)      String programCode
) {}
