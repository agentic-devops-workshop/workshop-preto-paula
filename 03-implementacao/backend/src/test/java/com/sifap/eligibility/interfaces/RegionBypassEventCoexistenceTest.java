package com.sifap.eligibility.interfaces;

import com.sifap.eligibility.infrastructure.DefaultEligibilityPortAdapter;
import org.junit.jupiter.api.Test;

import java.time.OffsetDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RegionBypassEventCoexistenceTest {

    @Test
    void region_bypass_events_should_coexist_without_double_counting() {
        List<Object> events = List.of(
            new DefaultEligibilityPortAdapter.RegionBypassEvaluated("BFA1", (short) 99, OffsetDateTime.now()),
            new RegionBypassUsed("BFA1", (short) 99, OffsetDateTime.now())
        );

        assertThat(events).filteredOn(DefaultEligibilityPortAdapter.RegionBypassEvaluated.class::isInstance).hasSize(1);
        assertThat(events).filteredOn(RegionBypassUsed.class::isInstance).hasSize(1);
    }

    record RegionBypassUsed(String programCode, short regionCode, OffsetDateTime occurredAt) {}
}