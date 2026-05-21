package com.sifap.socialprogram.interfaces.dto;

import com.sifap.socialprogram.domain.ProgramStatus;
import com.sifap.socialprogram.domain.ProgramType;
import com.sifap.socialprogram.domain.SocialProgram;

import java.math.BigDecimal;
import java.time.LocalDate;

public record SocialProgramDto(
    Long id,
    String code,
    ProgramType type,
    BigDecimal baseAmount,
    BigDecimal adjustmentFactor,
    ProgramStatus status,
    LocalDate effectiveFrom,
    LocalDate effectiveUntil,
    Long version
) {
    public static SocialProgramDto from(SocialProgram p) {
        return new SocialProgramDto(
            p.getId(), p.getCode(), p.getType(), p.getBaseAmount(), p.getAdjustmentFactor(),
            p.getStatus(), p.getEffectiveFrom(), p.getEffectiveUntil(), p.getVersion());
    }
}
