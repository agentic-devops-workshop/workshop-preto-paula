package com.sifap.eligibility.infrastructure;

import com.sifap.eligibility.application.ports.ProgramCriteriaPort;
import com.sifap.eligibility.domain.EligibilityCriteria;
import com.sifap.eligibility.domain.EligibilityResult;
import com.sifap.eligibility.domain.Reason;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class DefaultEligibilityPortAdapterTest {

    private final ProgramCriteriaPort criteriaPort = mock(ProgramCriteriaPort.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final DefaultEligibilityPortAdapter adapter = new DefaultEligibilityPortAdapter(criteriaPort, events);

    @Test
    void validate_should_publish_bypass_event_without_program_lookup_when_region_is_99() {
        EligibilityResult result = adapter.validate(
            "BFA1", LocalDate.of(1990, 1, 1), BigDecimal.ZERO, 0, (short) 99);

        assertThat(result).isInstanceOf(EligibilityResult.EligibleByBypass.class);
        verify(criteriaPort, never()).findByCode(any());
        verify(events).publishEvent(any(DefaultEligibilityPortAdapter.RegionBypassEvaluated.class));
    }

    @Test
    void validate_should_return_invalid_input_without_program_lookup_when_region_is_unknown() {
        EligibilityResult result = adapter.validate(
            "BFA1", LocalDate.of(1990, 1, 1), BigDecimal.ZERO, 0, (short) 50);

        assertThat(result).isInstanceOf(EligibilityResult.Ineligible.class);
        assertThat(((EligibilityResult.Ineligible) result).reason()).isEqualTo(Reason.INVALID_INPUT);
        verify(criteriaPort, never()).findByCode(any());
        verify(events, never()).publishEvent(any());
    }

    @Test
    void validate_should_lookup_program_for_standard_region_after_prevalidation() {
        when(criteriaPort.findByCode("BFA1")).thenReturn(Optional.of(
            new EligibilityCriteria("BFA1", 'A', new BigDecimal("600"), true, false)));

        EligibilityResult result = adapter.validate(
            "BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("900"), 0, (short) 26);

        assertThat(result).isInstanceOf(EligibilityResult.Ineligible.class);
        assertThat(((EligibilityResult.Ineligible) result).reason()).isEqualTo(Reason.INCOME_AND_NO_DEPENDENTS);
        verify(criteriaPort).findByCode("BFA1");
    }
}