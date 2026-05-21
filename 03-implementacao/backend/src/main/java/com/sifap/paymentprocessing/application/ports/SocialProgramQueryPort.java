package com.sifap.paymentprocessing.application.ports;

import java.math.BigDecimal;
import java.util.Optional;

/** Read-only port into the SocialProgramRegistry context. */
public interface SocialProgramQueryPort {

    record ProgramView(
        String code,
        String type,                   // 'A', 'P', 'T'
        BigDecimal baseAmount,
        BigDecimal adjustmentFactor,
        BigDecimal regionalFactor,     // for the beneficiary's UF, pre-resolved
        boolean active
    ) {
        public boolean isTypeA() { return "A".equals(type); }
    }

    Optional<ProgramView> findActiveByCode(String programCode);

    /** Resolve regional factor for the given (program, region) tuple (BR-012). */
    BigDecimal regionalFactor(String programCode, short regionCode);
}
