package com.sifap.eligibility.application.ports;

import com.sifap.eligibility.domain.EligibilityCriteria;

import java.util.Optional;

/**
 * Read-only port to SocialProgramRegistry. Implementation lives in
 * feature 003; absent when 003 is not merged yet — caller handles
 * empty as {@code PROGRAM_NOT_FOUND}.
 */
public interface ProgramCriteriaPort {
    Optional<EligibilityCriteria> findByCode(String programCode);
}
