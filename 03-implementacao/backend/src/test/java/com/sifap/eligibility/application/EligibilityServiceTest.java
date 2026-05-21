package com.sifap.eligibility.application;

import com.sifap.eligibility.api.BeneficiaryEligibilityPort;
import com.sifap.eligibility.api.EligibilityResult;
import com.sifap.eligibility.interfaces.dto.Region99BeneficiaryDto;
import com.sifap.eligibility.interfaces.dto.Region99ReportDto;
import com.sifap.eligibility.interfaces.dto.SimulateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.RowMapper;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class EligibilityServiceTest {

    private final BeneficiaryEligibilityPort eligibilityPort = mock(BeneficiaryEligibilityPort.class);
    private final JdbcOperations jdbcOperations = mock(JdbcOperations.class);
    private final ApplicationEventPublisher events = mock(ApplicationEventPublisher.class);
    private final EligibilityService service = new EligibilityService(eligibilityPort, jdbcOperations, events);

    @Test
    void simulate_should_delegate_to_published_port() {
        SimulateRequest request = new SimulateRequest("BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 2, (short) 26);
        when(eligibilityPort.validate("BFA1", request.birthDate(), request.familyIncome(), 2, (short) 26))
            .thenReturn(new EligibilityResult.Eligible());

        EligibilityResult result = service.simulate(request);

        assertThat(result).isInstanceOf(EligibilityResult.Eligible.class);
        verify(eligibilityPort).validate("BFA1", request.birthDate(), request.familyIncome(), 2, (short) 26);
    }

    @Test
    void region99Report_should_query_payment_rows_when_cycle_id_is_present() {
        Region99BeneficiaryDto row = Region99BeneficiaryDto.from("12345678901", 36, LocalDate.of(2026, 5, 20), false);
        when(jdbcOperations.query(contains("from payment"), any(RowMapper.class), eq(42L), eq(1_000)))
            .thenReturn(List.of(row));

        Region99ReportDto report = service.region99Report(42L, null, null, false);

        assertThat(report.cycleId()).isEqualTo(42L);
        assertThat(report.region99Count()).isEqualTo(1L);
        assertThat(report.beneficiaries()).containsExactly(row);
    }

    @Test
    void region99Report_should_reject_ambiguous_filters() {
        assertThatThrownBy(() -> service.region99Report(42L, "2026-05", "BFA1", false))
            .isInstanceOf(IllegalArgumentException.class)
            .hasMessageContaining("exactly one filter mode");
    }
}