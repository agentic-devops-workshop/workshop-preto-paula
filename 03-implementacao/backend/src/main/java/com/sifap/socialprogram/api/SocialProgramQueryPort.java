package com.sifap.socialprogram.api;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Published port — the ONLY symbol other bounded contexts may import
 * from SocialProgramRegistry. Consumed by {@code PaymentCycle} (feature
 * 001). Mirrors the contract declared in
 * {@code com.sifap.paymentprocessing.application.ports.SocialProgramQueryPort}.
 *
 * <p>The contract test {@code SocialProgramPortContractTest} verifies
 * the two interfaces are signature-compatible after both branches merge
 * to {@code develop}.
 */
public interface SocialProgramQueryPort {

    record ProgramView(
        String code,
        String type,
        BigDecimal baseAmount,
        BigDecimal adjustmentFactor,
        BigDecimal regionalFactor,
        boolean active
    ) {
        public boolean isTypeA() { return "A".equals(type); }
    }

    Optional<ProgramView> findActiveByCode(String programCode);

    BigDecimal regionalFactor(String programCode, short regionCode);
}
