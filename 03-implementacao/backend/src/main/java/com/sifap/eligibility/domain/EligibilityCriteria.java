package com.sifap.eligibility.domain;

import java.math.BigDecimal;

/**
 * Pre-resolved program data needed by the validator. Provided by
 * {@code ProgramCriteriaPort} so the validator stays pure.
 *
 * @param programCode     4-character program code
 * @param type            'A', 'P' or 'T'
 * @param incomeMax       max family income for type A (BR-025); ignored for other types
 * @param active          whether the program is in Active status
 * @param retired         whether the program is Retired (terminal — short-circuit Ineligible)
 */
public record EligibilityCriteria(
    String programCode,
    char type,
    BigDecimal incomeMax,
    boolean active,
    boolean retired
) {}
