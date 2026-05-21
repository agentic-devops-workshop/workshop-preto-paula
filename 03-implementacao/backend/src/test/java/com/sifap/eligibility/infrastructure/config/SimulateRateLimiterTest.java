package com.sifap.eligibility.infrastructure.config;

import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

import static org.assertj.core.api.Assertions.assertThat;

class SimulateRateLimiterTest {

    private final Clock fixedClock = Clock.fixed(Instant.parse("2026-05-21T00:00:00Z"), ZoneOffset.UTC);

    @Test
    void tryAcquire_should_limit_per_subject_per_minute() {
        SimulateRateLimiter limiter = new SimulateRateLimiter(2, fixedClock);

        assertThat(limiter.tryAcquire("operator-1")).isTrue();
        assertThat(limiter.tryAcquire("operator-1")).isTrue();
        assertThat(limiter.tryAcquire("operator-1")).isFalse();
    }

    @Test
    void tryAcquire_should_keep_subjects_independent() {
        SimulateRateLimiter limiter = new SimulateRateLimiter(1, fixedClock);

        assertThat(limiter.tryAcquire("operator-1")).isTrue();
        assertThat(limiter.tryAcquire("operator-2")).isTrue();
    }
}