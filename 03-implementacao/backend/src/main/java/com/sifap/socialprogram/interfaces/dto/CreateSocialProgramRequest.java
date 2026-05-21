package com.sifap.socialprogram.interfaces.dto;

import com.sifap.socialprogram.domain.ProgramType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.time.LocalDate;

public record CreateSocialProgramRequest(
    @NotBlank @Pattern(regexp = "[A-Z0-9]{4}") String code,
    @NotNull                                   ProgramType type,
    @NotNull @DecimalMin("0.00")               BigDecimal baseAmount,
    @NotNull @DecimalMin("-1.0") @DecimalMax("1.0") BigDecimal adjustmentFactor,
    @NotNull                                   LocalDate effectiveFrom
) {}
