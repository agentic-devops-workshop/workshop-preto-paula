package com.sifap.socialprogram.interfaces;

import com.sifap.socialprogram.application.SocialProgramService;
import com.sifap.socialprogram.domain.ProgramStatus;
import com.sifap.socialprogram.domain.SocialProgram;
import com.sifap.socialprogram.interfaces.dto.CreateSocialProgramRequest;
import com.sifap.socialprogram.interfaces.dto.SocialProgramDto;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.net.URI;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/social-programs")
public class SocialProgramController {

    private final SocialProgramService service;

    public SocialProgramController(SocialProgramService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<SocialProgramDto> create(@Valid @RequestBody CreateSocialProgramRequest req) {
        SocialProgram p = service.create(req.code(), req.type(), req.baseAmount(), req.adjustmentFactor(), req.effectiveFrom());
        return ResponseEntity
            .created(URI.create("/api/v1/social-programs/" + p.getCode()))
            .body(SocialProgramDto.from(p));
    }

    @GetMapping("/{code}")
    public ResponseEntity<SocialProgramDto> get(@PathVariable @Pattern(regexp = "[A-Z0-9]{4}") String code) {
        return ResponseEntity.ok(SocialProgramDto.from(service.findByCode(code)));
    }

    @PatchMapping("/{code}/adjustment-factor")
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<SocialProgramDto> updateAdjustment(@PathVariable String code,
                                                              @RequestBody @Valid AdjustmentBody body) {
        return ResponseEntity.ok(SocialProgramDto.from(
            service.updateAdjustmentFactor(code, body.adjustmentFactor())));
    }

    @PutMapping("/{code}/regional-factors")
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<SocialProgramDto> replaceRegionalFactors(@PathVariable String code,
                                                                   @RequestBody @Valid RegionalFactorsBody body) {
        return ResponseEntity.ok(SocialProgramDto.from(
            service.replaceRegionalFactors(code, body.factors())));
    }

    @PatchMapping("/{code}/status")
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<SocialProgramDto> changeStatus(@PathVariable String code,
                                                          @RequestBody @Valid StatusBody body) {
        return ResponseEntity.ok(SocialProgramDto.from(service.changeStatus(code, body.target())));
    }

    public record AdjustmentBody(@NotNull @DecimalMin("-1.0") @DecimalMax("1.0") BigDecimal adjustmentFactor) {}
    public record StatusBody(@NotNull ProgramStatus target) {}
    public record RegionalFactorsBody(@NotNull @Size(min = 27, max = 27) Map<String, BigDecimal> factors) {}
}
