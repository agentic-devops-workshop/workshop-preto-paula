package com.sifap.paymentprocessing.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Production-only guard for FR-030. The Quartz scheduler MUST be off
 * by default in production unless a SENARC ticket flips the flag.
 */
@Configuration
@Profile("prod")
public class CycleSchedulerBootGuard {

    @Value("${sifap.cycle.scheduler.enabled:false}")
    private boolean schedulerEnabled;

    @Value("${sifap.cycle.scheduler.senarcTicket:}")
    private String senarcTicket;

    @PostConstruct
    void enforce() {
        if (schedulerEnabled && (senarcTicket == null || senarcTicket.isBlank())) {
            throw new IllegalStateException(
                "FATAL: sifap.cycle.scheduler.enabled=true in production without a SENARC ticket. " +
                "Set sifap.cycle.scheduler.senarcTicket or keep the scheduler disabled. " +
                "See FR-030 / CycleSchedulerBootGuard.");
        }
    }
}
