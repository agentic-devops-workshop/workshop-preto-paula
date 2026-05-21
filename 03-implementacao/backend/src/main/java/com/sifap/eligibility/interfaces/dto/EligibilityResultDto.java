package com.sifap.eligibility.interfaces.dto;

import com.sifap.eligibility.api.EligibilityResult;
import com.sifap.eligibility.api.Reason;

public record EligibilityResultDto(String outcome, Short regionCode, Reason reason, String detail) {

    public static EligibilityResultDto from(EligibilityResult result) {
        return switch (result) {
            case EligibilityResult.Eligible ignored -> new EligibilityResultDto("Eligible", null, null, null);
            case EligibilityResult.EligibleByBypass bypass ->
                new EligibilityResultDto("EligibleByBypass", bypass.regionCode(), null, null);
            case EligibilityResult.Ineligible ineligible ->
                new EligibilityResultDto("Ineligible", null, ineligible.reason(), ineligible.detail());
        };
    }
}