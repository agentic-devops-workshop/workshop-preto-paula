package com.sifap.eligibility.infrastructure;

import com.sifap.eligibility.api.BeneficiaryEligibilityPort;
import com.sifap.eligibility.application.ports.ProgramCriteriaPort;
import com.sifap.eligibility.domain.EligibilityCriteria;
import com.sifap.eligibility.domain.EligibilityResult;
import com.sifap.eligibility.domain.EligibilityValidator;
import com.sifap.eligibility.domain.Reason;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Default adapter exposing the published port. Wires the pure validator
 * to the {@link ProgramCriteriaPort} and emits region-99 audit events.
 */
@Component
public class DefaultEligibilityPortAdapter implements BeneficiaryEligibilityPort {

    private static final Logger log = LoggerFactory.getLogger(DefaultEligibilityPortAdapter.class);

    private final ProgramCriteriaPort criteriaPort;
    private final ApplicationEventPublisher events;
    private final MetricsConfig.EligibilityMetrics metrics;

    public DefaultEligibilityPortAdapter(ProgramCriteriaPort criteriaPort,
                                         ApplicationEventPublisher events,
                                         MetricsConfig.EligibilityMetrics metrics) {
        this.criteriaPort = criteriaPort;
        this.events       = events;
        this.metrics = metrics;
    }

    @Override
    public EligibilityResult validate(String programCode, LocalDate birthDate,
                                       BigDecimal familyIncome, int dependents, short regionCode) {
        LocalDate referenceDate = LocalDate.now();
        EligibilityResult prechecked = EligibilityValidator.validate(
            null, birthDate, familyIncome, dependents, regionCode, referenceDate);

        if (prechecked instanceof EligibilityResult.EligibleByBypass
            || prechecked instanceof EligibilityResult.Ineligible ineligible
                && ineligible.reason() == Reason.INVALID_INPUT) {
            publishBypassEvent(programCode, prechecked);
            metrics.record(programCode, prechecked);
            return prechecked;
        }

        EligibilityCriteria criteria = (programCode == null)
            ? null
            : criteriaPort.findByCode(programCode).orElse(null);

        EligibilityResult result = EligibilityValidator.validate(
            criteria, birthDate, familyIncome, dependents, regionCode, referenceDate);

        publishBypassEvent(programCode, result);
        metrics.record(programCode, result);
        return result;
    }

    private void publishBypassEvent(String programCode, EligibilityResult result) {
        if (result instanceof EligibilityResult.EligibleByBypass(short bypassRegionCode)) {
            log.warn("Region-99 bypass evaluated. program={} regionCode={}", programCode, bypassRegionCode);
            events.publishEvent(new RegionBypassEvaluated(
                programCode, bypassRegionCode, OffsetDateTime.now()));
        }
    }

    /** Domain event published whenever the region-99 bypass is triggered (FR-006). */
    public record RegionBypassEvaluated(String programCode, short regionCode, OffsetDateTime occurredAt) {}
}
