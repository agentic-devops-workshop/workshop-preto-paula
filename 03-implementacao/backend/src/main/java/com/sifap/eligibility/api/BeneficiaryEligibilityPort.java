package com.sifap.eligibility.api;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.sifap.eligibility.domain.EligibilityResult;

/** Published port — the only symbol other contexts may import. Consumed by 001 and 002. */
public interface BeneficiaryEligibilityPort {

    EligibilityResult validate(
        String programCode,
        LocalDate birthDate,
        BigDecimal familyIncome,
        int dependents,
        short regionCode
    );
}
