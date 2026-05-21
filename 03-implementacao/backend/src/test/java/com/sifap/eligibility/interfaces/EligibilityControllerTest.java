package com.sifap.eligibility.interfaces;

import com.sifap.eligibility.api.BeneficiaryEligibilityPort;
import com.sifap.eligibility.application.EligibilityService;
import com.sifap.eligibility.infrastructure.config.SimulateRateLimiter;
import com.sifap.eligibility.interfaces.dto.Region99BeneficiaryDto;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class EligibilityControllerTest {

    private final JdbcOperations jdbcOperations = mock(JdbcOperations.class);
    private final EligibilityService service = new EligibilityService(
        mock(BeneficiaryEligibilityPort.class),
        jdbcOperations,
        mock(ApplicationEventPublisher.class)
    );
    private final EligibilityController controller = new EligibilityController(service, new SimulateRateLimiter(30));

    @Test
    void region99Report_should_return_masked_report_for_auditor() {
        when(jdbcOperations.query(contains("from payment"), any(RowMapper.class), eq(42L), eq(1_000)))
            .thenReturn(List.of(Region99BeneficiaryDto.from("12345678901", 36, LocalDate.of(2026, 5, 20), false)));

        var response = controller.region99Report(42L, null, null, false,
            new TestingAuthenticationToken("auditor", "n/a", "ROLE_AUD"));

        assertThat(response.region99Count()).isEqualTo(1L);
        assertThat(response.beneficiaries().getFirst().maskedCpf()).isEqualTo("XXX.XXX.XXX-01");
    }

    @Test
    void region99Report_should_reject_reveal_without_aud_role() {
        assertThatThrownBy(() -> controller.region99Report(42L, null, null, true,
            new TestingAuthenticationToken("admin", "n/a", "ROLE_ADM")))
            .isInstanceOf(AccessDeniedException.class);
    }

    @Test
    void region99Report_should_reject_missing_filter() {
        assertThatThrownBy(() -> controller.region99Report(null, null, null, false,
            new TestingAuthenticationToken("auditor", "n/a", "ROLE_AUD")))
            .isInstanceOf(IllegalArgumentException.class);
    }
}