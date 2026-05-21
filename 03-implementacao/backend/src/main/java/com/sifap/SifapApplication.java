package com.sifap;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot entry point for the SIFAP 2.0 modular monolith.
 *
 * <p>Bounded contexts live as sibling packages under {@code com.sifap.*}.
 * No microservices — see ADR-001 and the constitution Principle II.
 */
@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "securityAuditorAware", dateTimeProviderRef = "offsetDateTimeProvider")
@EnableScheduling
public class SifapApplication {
    public static void main(String[] args) {
        SpringApplication.run(SifapApplication.class, args);
    }
}
