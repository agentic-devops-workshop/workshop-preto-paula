package com.sifap.eligibility.interfaces;

import com.sifap.eligibility.application.EligibilityService;
import com.sifap.eligibility.infrastructure.config.SimulateRateLimiter;
import com.sifap.eligibility.interfaces.dto.EligibilityResultDto;
import com.sifap.eligibility.interfaces.dto.Region99ReportDto;
import com.sifap.eligibility.interfaces.dto.SimulateRequest;
import jakarta.validation.Valid;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/eligibility")
public class EligibilityController {

    private final EligibilityService eligibilityService;
    private final SimulateRateLimiter rateLimiter;

    public EligibilityController(EligibilityService eligibilityService, SimulateRateLimiter rateLimiter) {
        this.eligibilityService = eligibilityService;
        this.rateLimiter = rateLimiter;
    }

    @PostMapping("/simulate")
    @PreAuthorize("isAuthenticated()")
    public EligibilityResultDto simulate(@Valid @RequestBody SimulateRequest request, Authentication authentication) {
        requireAuthenticated(authentication);
        String subject = authentication == null ? null : authentication.getName();
        if (!rateLimiter.tryAcquire(subject)) {
            throw new RateLimitExceededException("Eligibility simulate limit exceeded: 30 calls/min per user");
        }
        return EligibilityResultDto.from(eligibilityService.simulate(request));
    }

    @GetMapping("/region-99-report")
    @PreAuthorize("hasAnyRole('ADM','AUD')")
    public Region99ReportDto region99Report(@RequestParam(required = false) Long cycleId,
                                            @RequestParam(required = false) String competence,
                                            @RequestParam(required = false) String programCode,
                                            @RequestParam(defaultValue = "false") boolean revealCpf,
                                            Authentication authentication) {
        requireAnyRole(authentication, "ROLE_ADM", "ROLE_AUD");
        if (revealCpf) {
            requireAnyRole(authentication, "ROLE_AUD");
        }
        return eligibilityService.region99Report(cycleId, competence, programCode, revealCpf);
    }

    private static void requireAuthenticated(Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("Authentication required");
        }
    }

    private static void requireAnyRole(Authentication authentication, String... roles) {
        requireAuthenticated(authentication);
        for (String role : roles) {
            if (authentication.getAuthorities().stream().map(GrantedAuthority::getAuthority).anyMatch(role::equals)) {
                return;
            }
        }
        throw new AccessDeniedException("Required role missing");
    }

    public static class RateLimitExceededException extends RuntimeException {
        public RateLimitExceededException(String message) {
            super(message);
        }
    }
}