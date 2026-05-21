package com.sifap.socialprogram.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SocialProgramTest {

    @Test
    void rejects_adjustment_factor_out_of_range() {
        assertThatThrownBy(() -> new SocialProgram(
            "BFA1", ProgramType.A, new BigDecimal("400"), new BigDecimal("1.5"), LocalDate.now()))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("adjustmentFactor");
    }

    @Test
    void replace_regional_factors_requires_27_ufs() {
        SocialProgram p = new SocialProgram("BFA1", ProgramType.A,
            new BigDecimal("400"), new BigDecimal("0.05"), LocalDate.now());
        Map<String, BigDecimal> incomplete = new HashMap<>();
        incomplete.put("SP", new BigDecimal("1.20"));
        assertThatThrownBy(() -> p.replaceRegionalFactors(incomplete))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("27");
    }

    @Test
    void replace_regional_factors_full_27_works_and_lookup_returns_value() {
        SocialProgram p = new SocialProgram("BFA1", ProgramType.A,
            new BigDecimal("400"), new BigDecimal("0.05"), LocalDate.now());
        Map<String, BigDecimal> full = new HashMap<>();
        SocialProgram.BRAZILIAN_UFS.forEach(uf -> full.put(uf, new BigDecimal("1.00")));
        full.put("SP", new BigDecimal("1.20"));
        p.replaceRegionalFactors(full);
        assertThat(p.regionalFactor("SP")).contains(new BigDecimal("1.20"));
        assertThat(p.regionalFactor("XX")).isEmpty();
    }

    @Test
    void retired_is_terminal() {
        SocialProgram p = new SocialProgram("BFA1", ProgramType.A,
            new BigDecimal("400"), new BigDecimal("0.05"), LocalDate.now());
        p.changeStatus(ProgramStatus.Suspended);
        p.changeStatus(ProgramStatus.Retired);
        assertThatThrownBy(() -> p.changeStatus(ProgramStatus.Active))
            .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> p.changeAdjustmentFactor(new BigDecimal("0.06")))
            .isInstanceOf(IllegalStateException.class);
    }
}
