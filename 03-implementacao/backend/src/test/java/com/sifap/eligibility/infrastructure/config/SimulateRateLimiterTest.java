package com.sifap.eligibility.infrastructure.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SimulateRateLimiterTest {

    @Test
    void tryAcquire_should_limit_per_subject_per_minute() {
        SimulateRateLimiter limiter = new SimulateRateLimiter(2);

        assertThat(limiter.tryAcquire("operator-1")).isTrue();
        assertThat(limiter.tryAcquire("operator-1")).isTrue();
        assertThat(limiter.tryAcquire("operator-1")).isFalse();
    }

    @Test
    void tryAcquire_should_keep_subjects_independent() {
        SimulateRateLimiter limiter = new SimulateRateLimiter(1);

        assertThat(limiter.tryAcquire("operator-1")).isTrue();
        assertThat(limiter.tryAcquire("operator-2")).isTrue();
    }
}