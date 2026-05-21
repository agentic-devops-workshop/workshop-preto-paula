package com.sifap.eligibility.performance;

import com.sifap.eligibility.domain.EligibilityCriteria;
import com.sifap.eligibility.domain.EligibilityValidator;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

import static org.assertj.core.api.Assertions.assertThat;

class EligibilityValidatorBench {

    @Test
    void validate_should_process_100000_calls_within_one_second() {
        EligibilityCriteria criteria = new EligibilityCriteria("BFA1", 'A', new BigDecimal("600"), true, false);
        LocalDate birthDate = LocalDate.of(1990, 1, 1);
        LocalDate referenceDate = LocalDate.of(2026, 6, 1);

        long started = System.nanoTime();
        for (int index = 0; index < 100_000; index++) {
            EligibilityValidator.validate(criteria, birthDate, new BigDecimal("550"), 1, (short) 26, referenceDate);
        }
        long elapsedMillis = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - started);

        assertThat(elapsedMillis).isLessThanOrEqualTo(1_000L);
    }
}