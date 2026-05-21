package com.sifap.paymentprocessing.interfaces;

import com.sifap.paymentprocessing.application.PaymentCycleService;
import com.sifap.paymentprocessing.application.PaymentCycleService.TriggerCommand;
import com.sifap.paymentprocessing.domain.PaymentCycle;
import com.sifap.paymentprocessing.interfaces.dto.PaymentCycleDto;
import com.sifap.paymentprocessing.interfaces.dto.TriggerCycleRequest;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/payment-cycles")
public class PaymentCycleController {

    private final PaymentCycleService service;

    public PaymentCycleController(PaymentCycleService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('OPR','ADM')")
    public ResponseEntity<PaymentCycleDto> trigger(@Valid @RequestBody TriggerCycleRequest req,
                                                   @AuthenticationPrincipal Jwt jwt) {
        String triggeredBy = jwt != null ? jwt.getSubject() : "anonymous";
        PaymentCycle cycle = service.trigger(new TriggerCommand(
            req.competence(), req.programCode(), triggeredBy,
            req.forceBackfill(), req.backfillReason()));
        return ResponseEntity
            .status(HttpStatus.ACCEPTED)
            .location(URI.create("/api/v1/payment-cycles/" + cycle.getId()))
            .body(PaymentCycleDto.from(cycle));
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentCycleDto> get(@PathVariable long id) {
        return ResponseEntity.ok(PaymentCycleDto.from(service.findById(id)));
    }

    @PostMapping("/{id}/cancel")
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<Void> cancel(@PathVariable long id, @RequestBody @NotBlank String reason) {
        service.cancel(id, reason);
        return ResponseEntity.accepted().build();
    }
}
