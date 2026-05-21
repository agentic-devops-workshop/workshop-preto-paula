package com.sifap.paymentprocessing.domain.calculation;

import com.sifap.paymentprocessing.domain.Competence;
import com.sifap.paymentprocessing.domain.Money;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Equivalence harness for FR-EQUIV-001..003.
 *
 * <p>For each row of the captured legacy fixture, the new
 * {@link PaymentFormula} must produce byte-identical gross and Christmas
 * allowance values. Failure here means we cannot deploy SIFAP 2.0
 * without changing an audited payment value — which is forbidden by
 * the constitution Principle I.
 */
class PaymentCycleEquivalenceTest {

    private static final LocalDate REFERENCE = LocalDate.of(2026, 5, 20);

    @ParameterizedTest(name = "{0} {1} → gross={10} xmas={11}")
    @CsvFileSource(
        resources = "/fixtures/legacy-fixture-2026-05.csv",
        numLinesToSkip = 1
    )
    void new_implementation_matches_legacy_fixture(
        String cpf,
        String competence,
        String programCode,
        String programType,
        BigDecimal base,
        BigDecimal regionFactor,
        int dependents,
        BigDecimal familyIncome,
        LocalDate birthDate,
        BigDecimal adjustmentFactor,
        BigDecimal expectedGross,
        BigDecimal expectedChristmas,
        String paymentType
    ) {
        PaymentFormula.Input in = new PaymentFormula.Input(
            Money.of(base),
            regionFactor,
            dependents,
            familyIncome,
            birthDate,
            adjustmentFactor,
            "A".equals(programType),
            Competence.parse(competence),
            REFERENCE
        );

        PaymentFormula.Result r = PaymentFormula.compute(in);

        assertThat(r.grossAmount())
            .as("CPF %s competence %s gross mismatch — equivalence broken (FR-EQUIV-001)", cpf, competence)
            .isEqualTo(Money.of(expectedGross));
        assertThat(r.christmasAllowance())
            .as("CPF %s competence %s christmas mismatch — equivalence broken (FR-EQUIV-001)", cpf, competence)
            .isEqualTo(Money.of(expectedChristmas));
    }
}
