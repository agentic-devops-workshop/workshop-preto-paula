package com.sifap.paymentprocessing.domain.calculation;

import com.sifap.paymentprocessing.domain.Competence;
import com.sifap.paymentprocessing.domain.Money;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

class PaymentFormulaTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 6, 1);

    @Test
    void monthly_applies_all_five_factors_with_truncation() {
        PaymentFormula.Input in = new PaymentFormula.Input(
            Money.of("400.00"),
            new BigDecimal("1.20"),         // FREG
            2,                              // dependents → FFAM = 1.10
            new BigDecimal("250.00"),       // income → FRND = 1.00
            LocalDate.of(1980, 5, 14),      // age 46 → FIDADE = 1.00
            new BigDecimal("0.05"),         // adj → k = 1 + 0.05 × 0.347215 ≈ 1.01736075
            false,
            Competence.of(2026, 6),
            TODAY
        );

        PaymentFormula.Result r = PaymentFormula.compute(in);

        // 400 × 1.20 × 1.10 × 1.00 × 1.00 × 1.01736075 ≈ 537.166...
        assertThat(r.grossAmount()).isEqualTo(Money.of("537.16"));    // truncated DOWN, BR-020
        assertThat(r.christmasAllowance()).isEqualTo(Money.ZERO);
    }

    @Test
    void december_omits_family_and_income_factors_BR018() {
        PaymentFormula.Input in = new PaymentFormula.Input(
            Money.of("400.00"),
            new BigDecimal("1.20"),
            10,                              // would normally bump FFAM
            new BigDecimal("9999"),          // would normally crush FRND
            LocalDate.of(1980, 5, 14),
            new BigDecimal("0.05"),
            false,
            Competence.of(2026, 12),
            TODAY
        );

        PaymentFormula.Result r = PaymentFormula.compute(in);

        // 400 × 1.20 × 1.00 × 1.01736075 ≈ 488.333... → 488.33
        assertThat(r.grossAmount()).isEqualTo(Money.of("488.33"));
        assertThat(r.christmasAllowance()).isEqualTo(Money.ZERO);   // not type-A
    }

    @Test
    void typeA_december_adds_15percent_christmas_allowance_BR019() {
        PaymentFormula.Input in = new PaymentFormula.Input(
            Money.of("400.00"),
            new BigDecimal("1.00"),
            0,
            new BigDecimal("0"),
            LocalDate.of(1980, 5, 14),
            BigDecimal.ZERO,
            true,                            // type-A
            Competence.of(2026, 12),
            TODAY
        );

        PaymentFormula.Result r = PaymentFormula.compute(in);

        assertThat(r.christmasAllowance()).isEqualTo(Money.of("60.00"));   // 400 × 0.15
    }

    @Test
    void mainframe_truncation_never_rounds_up() {
        // build a case whose pre-truncation result ends in 0.0099
        PaymentFormula.Input in = new PaymentFormula.Input(
            Money.of("100.00"),
            new BigDecimal("1.0099"),
            0,
            BigDecimal.ZERO,
            LocalDate.of(1990, 1, 1),
            BigDecimal.ZERO,
            false,
            Competence.of(2026, 7),
            TODAY
        );
        PaymentFormula.Result r = PaymentFormula.compute(in);
        assertThat(r.grossAmount()).isEqualTo(Money.of("100.99"));   // not 101.00
    }
}
