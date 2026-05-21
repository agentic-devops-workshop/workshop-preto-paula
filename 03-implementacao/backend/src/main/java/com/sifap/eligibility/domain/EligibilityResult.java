package com.sifap.eligibility.domain;

/** Result of an eligibility evaluation. Closed hierarchy — exhaustive pattern matching. */
public sealed interface EligibilityResult
    permits EligibilityResult.Eligible,
            EligibilityResult.EligibleByBypass,
            EligibilityResult.Ineligible {

    record Eligible() implements EligibilityResult {}

    /** Granted by BR-024 region-99 bypass (MYS-008). Always emits a WARN audit event. */
    record EligibleByBypass(short regionCode) implements EligibilityResult {}

    record Ineligible(Reason reason, String detail) implements EligibilityResult {}

    default boolean isEligible() { return !(this instanceof Ineligible); }
    default boolean isBypass()   { return this instanceof EligibleByBypass; }
}
