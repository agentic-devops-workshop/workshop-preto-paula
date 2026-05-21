package com.sifap.paymentprocessing.domain.calculation;

import com.sifap.paymentprocessing.domain.Competence;
import com.sifap.paymentprocessing.domain.Money;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Core payment formula (BR-017). Pure function — no I/O, no state.
 *
 * <pre>
 * Monthly:   VLR = BASE × FREG × FFAM × FRND × FIDADE × kFactor
 * December:  VLR = BASE × FREG × FIDADE × kFactor       (FFAM and FRND omitted, BR-018)
 * December + type-A: also add christmasAllowance = trunc(BASE × 0.15)
 * All steps end with mainframe DOWN truncation to 2 decimals.
 * </pre>
 */
public final class PaymentFormula {

    public record Result(Money grossAmount, Money christmasAllowance, Factors factors) {}

    public record Factors(
        BigDecimal regional,
        BigDecimal family,
        BigDecimal income,
        BigDecimal age,
        BigDecimal kFactor,
        Money base
    ) {}

    public record Input(
        Money base,
        BigDecimal regionalFactor,
        int dependents,
        BigDecimal familyIncome,
        LocalDate birthDate,
        BigDecimal adjustmentFactor,
        boolean isTypeA,
        Competence competence,
        LocalDate referenceDate
    ) {}

    private PaymentFormula() {}

    public static Result compute(Input in) {
        BigDecimal age = AgeFactor.compute(in.birthDate(), in.referenceDate());
        BigDecimal k   = KFactor.compute(in.adjustmentFactor());

        Money gross;
        Factors factors;

        if (in.competence().isDecember()) {
            // BR-018: 13th payment — skip family and income factors.
            gross = in.base()
                .times(in.regionalFactor())
                .times(age)
                .times(k);
            factors = new Factors(in.regionalFactor(), null, null, age, k, in.base());
        } else {
            BigDecimal family = FamilyFactor.compute(in.dependents());
            BigDecimal income = IncomeFactor.compute(in.familyIncome());
            gross = in.base()
                .times(in.regionalFactor())
                .times(family)
                .times(income)
                .times(age)
                .times(k);
            factors = new Factors(in.regionalFactor(), family, income, age, k, in.base());
        }

        Money christmas = ChristmasAllowance.compute(in.isTypeA(), in.competence().isDecember(), in.base());
        return new Result(gross, christmas, factors);
    }
}
