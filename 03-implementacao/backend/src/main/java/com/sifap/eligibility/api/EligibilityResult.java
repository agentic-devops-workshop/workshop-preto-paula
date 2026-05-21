package com.sifap.eligibility.api;

/** Public result contract returned by the eligibility port to sibling modules. */
public sealed interface EligibilityResult
    permits EligibilityResult.Eligible,
            EligibilityResult.EligibleByBypass,
            EligibilityResult.Ineligible {

    record Eligible() implements EligibilityResult {}

    record EligibleByBypass(short regionCode) implements EligibilityResult {}

    record Ineligible(Reason reason, String detail) implements EligibilityResult {}

    default boolean isEligible() { return !(this instanceof Ineligible); }
    default boolean isBypass()   { return this instanceof EligibleByBypass; }
}