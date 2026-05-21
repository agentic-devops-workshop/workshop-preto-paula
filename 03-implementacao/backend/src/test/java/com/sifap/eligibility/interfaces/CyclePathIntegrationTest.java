package com.sifap.eligibility.interfaces;

import com.sifap.eligibility.application.ports.ProgramCriteriaPort;
import com.sifap.eligibility.domain.EligibilityCriteria;
import com.sifap.eligibility.api.EligibilityResult;
import com.sifap.eligibility.infrastructure.DefaultEligibilityPortAdapter;
import com.sifap.eligibility.infrastructure.MetricsConfig;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

class CyclePathIntegrationTest {

    @Test
    void cycle_path_should_skip_ineligible_and_count_region99_bypass() {
        ProgramCriteriaPort criteriaPort = code -> Optional.of(new EligibilityCriteria(
            code, 'A', new BigDecimal("600"), true, false));
        List<Object> events = new ArrayList<>();
        ApplicationEventPublisher publisher = events::add;
        DefaultEligibilityPortAdapter adapter = new DefaultEligibilityPortAdapter(
            criteriaPort,
            publisher,
            new MetricsConfig.EligibilityMetrics(new SimpleMeterRegistry())
        );

        List<EligibilityResult> results = List.of(
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("900"), 0, (short) 26),
            adapter.validate("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("900"), 0, (short) 99)
        );

        assertThat(results).filteredOn(EligibilityResult::isEligible).hasSize(9);
        assertThat(results).filteredOn(result -> result instanceof EligibilityResult.Ineligible).hasSize(1);
        assertThat(results).filteredOn(EligibilityResult::isBypass).hasSize(1);
        assertThat(events).filteredOn(DefaultEligibilityPortAdapter.RegionBypassEvaluated.class::isInstance).hasSize(1);
    }
}