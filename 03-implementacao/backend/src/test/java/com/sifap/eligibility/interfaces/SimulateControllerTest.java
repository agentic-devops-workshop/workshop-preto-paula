package com.sifap.eligibility.interfaces;

import com.sifap.eligibility.api.BeneficiaryEligibilityPort;
import com.sifap.eligibility.application.EligibilityService;
import com.sifap.eligibility.domain.EligibilityResult;
import com.sifap.eligibility.infrastructure.config.SimulateRateLimiter;
import com.sifap.eligibility.interfaces.dto.EligibilityResultDto;
import com.sifap.eligibility.interfaces.dto.SimulateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.authentication.TestingAuthenticationToken;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class SimulateControllerTest {

    private final BeneficiaryEligibilityPort eligibilityPort = mock(BeneficiaryEligibilityPort.class);
    private final EligibilityService service = new EligibilityService(
        eligibilityPort,
        mock(JdbcOperations.class),
        mock(ApplicationEventPublisher.class)
    );
    private final SimulateRequest request = new SimulateRequest(
        "BFA1", LocalDate.of(1990, 1, 1), new BigDecimal("550"), 2, (short) 26);

    @Test
    void simulate_should_return_happy_path_result_when_authenticated() {
        when(eligibilityPort.validate("BFA1", request.birthDate(), request.familyIncome(), 2, (short) 26))
            .thenReturn(new EligibilityResult.Eligible());
        EligibilityController controller = new EligibilityController(service, new SimulateRateLimiter(30));

        EligibilityResultDto response = controller.simulate(request, new TestingAuthenticationToken("operator-1", "n/a", "ROLE_OPR"));

        assertThat(response.outcome()).isEqualTo("Eligible");
    }

    @Test
    void simulate_should_reject_anonymous_calls() {
        EligibilityController controller = new EligibilityController(service, new SimulateRateLimiter(30));

        assertThatThrownBy(() -> controller.simulate(request, null))
            .isInstanceOf(AuthenticationCredentialsNotFoundException.class);
    }

    @Test
    void simulate_should_return_rate_limit_exception_after_limit_is_exceeded() {
        when(eligibilityPort.validate("BFA1", request.birthDate(), request.familyIncome(), 2, (short) 26))
            .thenReturn(new EligibilityResult.Eligible());
        EligibilityController controller = new EligibilityController(service, new SimulateRateLimiter(1));
        TestingAuthenticationToken authentication = new TestingAuthenticationToken("operator-1", "n/a", "ROLE_OPR");

        controller.simulate(request, authentication);

        assertThatThrownBy(() -> controller.simulate(request, authentication))
            .isInstanceOf(EligibilityController.RateLimitExceededException.class);
    }
}