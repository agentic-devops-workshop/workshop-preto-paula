package com.sifap.beneficiary.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;

/**
 * Production-only guard for ADR-008. Refuses boot when
 * {@code sifap.cpf.allowTestCpfs=true} in production.
 */
@Configuration
@Profile("prod")
public class CpfBackdoorBootGuard {

    @Value("${sifap.cpf.allowTestCpfs:false}")
    private boolean allowTestCpfs;

    @PostConstruct
    void enforce() {
        if (allowTestCpfs) {
            throw new IllegalStateException(
                "FATAL: sifap.cpf.allowTestCpfs MUST be false in production. " +
                "See ADR-008. Refusing to start.");
        }
    }
}
