package com.sifap.paymentprocessing.application;

import com.sifap.paymentprocessing.application.ports.AuditEventPublisher;
import com.sifap.paymentprocessing.application.ports.SocialProgramQueryPort;
import com.sifap.paymentprocessing.domain.Competence;
import com.sifap.paymentprocessing.domain.CycleStatus;
import com.sifap.paymentprocessing.domain.PaymentCycle;
import com.sifap.paymentprocessing.infrastructure.persistence.PaymentCycleRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.UUID;

/**
 * Use cases for {@link PaymentCycle} — trigger, cancel, resume.
 *
 * <p>The actual row generation is delegated to a Spring Batch job
 * (see {@code PaymentCycleJobConfig}). This service owns the
 * preconditions, status transitions and idempotency translation.
 */
@Service
@Transactional
public class PaymentCycleService {

    private static final Logger log = LoggerFactory.getLogger(PaymentCycleService.class);
    private static final int BACKFILL_CUTOFF_MONTHS = 12;   // CL-004

    private final PaymentCycleRepository repository;
    private final SocialProgramQueryPort programs;
    private final AuditEventPublisher auditPublisher;
    private final CycleJobLauncher jobLauncher;

    public PaymentCycleService(PaymentCycleRepository repository,
                               SocialProgramQueryPort programs,
                               AuditEventPublisher auditPublisher,
                               CycleJobLauncher jobLauncher) {
        this.repository     = repository;
        this.programs       = programs;
        this.auditPublisher = auditPublisher;
        this.jobLauncher    = jobLauncher;
    }

    public PaymentCycle trigger(TriggerCommand cmd) {
        Competence competence = Competence.parse(cmd.competence());

        if (competence.isFuture(LocalDate.now())) {
            throw new IllegalArgumentException(
                "Competence %s is in the future".formatted(competence));   // FR-021
        }
        if (competence.isOlderThanMonths(BACKFILL_CUTOFF_MONTHS, LocalDate.now())) {
            if (!cmd.forceBackfill()) {
                throw new IllegalStateException(
                    "Competence %s is older than %d months — set forceBackfill=true with a reason"
                        .formatted(competence, BACKFILL_CUTOFF_MONTHS));   // FR-022
            }
            if (cmd.backfillReason() == null || cmd.backfillReason().isBlank()) {
                throw new IllegalArgumentException("backfillReason is required when forceBackfill=true");
            }
        }

        SocialProgramQueryPort.ProgramView program = programs.findActiveByCode(cmd.programCode())
            .orElseThrow(() -> new IllegalStateException(
                "Program %s is not active".formatted(cmd.programCode())));   // FR-003
        if (!program.active()) {
            throw new IllegalStateException("Program %s is not active".formatted(cmd.programCode()));
        }

        PaymentCycle cycle = new PaymentCycle(
            competence, cmd.programCode(), cmd.triggeredBy(),
            UUID.randomUUID(), cmd.forceBackfill(), cmd.backfillReason());
        try {
            cycle = repository.saveAndFlush(cycle);
        } catch (DataIntegrityViolationException ex) {
            // FR-002 — translate uq_cycle violation to 409
            throw new CycleAlreadyExistsException(competence.asText(), cmd.programCode(), ex);
        }

        log.info("Triggered cycle id={} competence={} program={} correlationId={}",
            cycle.getId(), cycle.getCompetence(), cycle.getProgramCode(), cycle.getCorrelationId());

        jobLauncher.launch(cycle.getId());
        return cycle;
    }

    public void cancel(long cycleId, String reason) {
        PaymentCycle cycle = repository.findById(cycleId)
            .orElseThrow(() -> new CycleNotFoundException(cycleId));
        if (reason == null || reason.isBlank()) {
            throw new IllegalArgumentException("reason is required");
        }
        cycle.abort();   // throws IllegalStateException if not Running
        jobLauncher.stop(cycleId);
        log.warn("Aborted cycle id={} reason={}", cycleId, reason);
    }

    @Transactional(readOnly = true)
    public PaymentCycle findById(long id) {
        return repository.findById(id).orElseThrow(() -> new CycleNotFoundException(id));
    }

    public record TriggerCommand(
        String competence,
        String programCode,
        String triggeredBy,
        boolean forceBackfill,
        String backfillReason
    ) {}

    public static class CycleAlreadyExistsException extends RuntimeException {
        public CycleAlreadyExistsException(String competence, String program, Throwable cause) {
            super("Cycle for competence=%s program=%s already exists".formatted(competence, program), cause);
        }
    }

    public static class CycleNotFoundException extends RuntimeException {
        public CycleNotFoundException(long id) { super("Cycle " + id + " not found"); }
    }
}
