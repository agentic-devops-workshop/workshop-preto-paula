package com.sifap.beneficiary.interfaces;

import com.sifap.beneficiary.application.BeneficiaryService;
import com.sifap.beneficiary.domain.Beneficiary;
import com.sifap.beneficiary.interfaces.dto.BeneficiaryDto;
import com.sifap.beneficiary.interfaces.dto.ChangeLifecycleStatusRequest;
import com.sifap.beneficiary.interfaces.dto.RegisterBeneficiaryRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;

@RestController
@RequestMapping("/api/v1/beneficiaries")
public class BeneficiaryController {

    private final BeneficiaryService service;

    public BeneficiaryController(BeneficiaryService service) { this.service = service; }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADM','OPR')")
    public ResponseEntity<BeneficiaryDto> register(@Valid @RequestBody RegisterBeneficiaryRequest req) {
        Beneficiary b = service.register(req.cpf(), req.name(), req.birthDate(), req.sex(), req.programCode());
        return ResponseEntity
            .created(URI.create("/api/v1/beneficiaries/" + b.getCpf()))
            .body(BeneficiaryDto.from(b));
    }

    @GetMapping("/{cpf}")
    public ResponseEntity<BeneficiaryDto> findByCpf(@PathVariable String cpf) {
        return ResponseEntity.ok(BeneficiaryDto.from(service.findByCpf(cpf)));
    }

    @PatchMapping("/{cpf}/status")
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<BeneficiaryDto> changeStatus(@PathVariable String cpf,
                                                       @Valid @RequestBody ChangeLifecycleStatusRequest req) {
        service.changeLifecycleStatus(cpf, req.target(), req.reason());
        return ResponseEntity.ok(BeneficiaryDto.from(service.findByCpf(cpf)));
    }

    @DeleteMapping("/{cpf}")
    @PreAuthorize("hasRole('ADM')")
    public ResponseEntity<Void> cancel(@PathVariable String cpf, @RequestParam String reason) {
        service.cancel(cpf, reason);
        return ResponseEntity.noContent().build();
    }
}
