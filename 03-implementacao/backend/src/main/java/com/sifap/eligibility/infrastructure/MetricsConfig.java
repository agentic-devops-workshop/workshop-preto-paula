package com.sifap.eligibility.infrastructure;

import com.sifap.eligibility.domain.EligibilityResult;
import io.micrometer.core.instrument.MeterRegistry;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class MetricsConfig {

    @Bean
    EligibilityMetrics eligibilityMetrics(MeterRegistry meterRegistry) {
        return new EligibilityMetrics(meterRegistry);
    }

    public static class EligibilityMetrics {
        private final MeterRegistry meterRegistry;

        public EligibilityMetrics(MeterRegistry meterRegistry) {
            this.meterRegistry = meterRegistry;
        }

        public void record(String programCode, EligibilityResult result) {
            String outcome = switch (result) {
                case EligibilityResult.Eligible ignored -> "Eligible";
                case EligibilityResult.EligibleByBypass ignored -> "EligibleByBypass";
                case EligibilityResult.Ineligible ignored -> "Ineligible";
            };
            meterRegistry.counter("sifap.eligibility.evaluated.count", "result", outcome).increment();
            if (result instanceof EligibilityResult.EligibleByBypass) {
                meterRegistry.counter("sifap.eligibility.region99.count", "program", normalizeProgram(programCode)).increment();
            }
        }

        private static String normalizeProgram(String programCode) {
            return programCode == null || programCode.isBlank() ? "UNKNOWN" : programCode;
        }
    }
}