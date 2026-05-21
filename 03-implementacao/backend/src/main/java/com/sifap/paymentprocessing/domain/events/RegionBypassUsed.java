package com.sifap.paymentprocessing.domain.events;

import java.time.OffsetDateTime;
import java.util.UUID;

/** Emitted at WARN severity for every region-99 row (BR-024, MYS-008). */
public record RegionBypassUsed(
    long cycleId,
    UUID correlationId,
    String maskedCpf,
    String competence,
    OffsetDateTime occurredAt
) {}
