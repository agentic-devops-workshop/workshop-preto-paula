package com.sifap.eligibility.interfaces.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SimulateRequest(
    @NotBlank @Pattern(regexp = "[A-Z0-9]{4}") String programCode,
    @NotNull LocalDate birthDate,
    @NotNull @Min(0) BigDecimal familyIncome,
    @Min(0) int dependents,
    short regionCode
) {}