package com.sifap.paymentprocessing.interfaces.dto;

import com.sifap.paymentprocessing.domain.CycleStatus;
import com.sifap.paymentprocessing.domain.PaymentCycle;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.UUID;

public record PaymentCycleDto(
    Long id,
    String competence,
    String programCode,
    CycleStatus status,
    UUID correlationId,
    String triggeredBy,
    OffsetDateTime startedAt,
    OffsetDateTime completedAt,
    long generatedCount,
    long skippedCount,
    long errorCount,
    long region99BypassCount,
    BigDecimal totalGrossAmount,
    boolean forceBackfill,
    String backfillReason
) {
    public static PaymentCycleDto from(PaymentCycle c) {
        return new PaymentCycleDto(
            c.getId(), c.getCompetence(), c.getProgramCode(), c.getStatus(),
            c.getCorrelationId(), c.getTriggeredBy(),
            c.getStartedAt(), c.getCompletedAt(),
            c.getGeneratedCount(), c.getSkippedCount(),
            c.getErrorCount(), c.getRegion99BypassCount(),
            c.getTotalGrossAmount(),
            c.isForceBackfill(), c.getBackfillReason()
        );
    }
}
