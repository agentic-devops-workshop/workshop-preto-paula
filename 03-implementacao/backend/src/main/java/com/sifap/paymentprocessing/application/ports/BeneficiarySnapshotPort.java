package com.sifap.paymentprocessing.application.ports;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.stream.Stream;

/**
 * Read-only port to the Beneficiary bounded context.
 * Implementations live in the {@code beneficiary.api} package; this
 * keeps PaymentProcessing decoupled from BeneficiaryManagement internals.
 */
public interface BeneficiarySnapshotPort {

    /** A row in the snapshot used by the payment cycle. Pure data, no behavior. */
    record Snapshot(
        long id,
        String cpf,
        LocalDate birthDate,
        short regionCode,
        int dependentCount,
        BigDecimal familyIncome,
        String lifecycleStatus,
        boolean legacyBackdoor
    ) {}

    /** Stream all beneficiaries enrolled in {@code programCode}, sorted ascending by CPF (BR-033). */
    Stream<Snapshot> streamActiveByProgramSortedByCpf(String programCode);
}
