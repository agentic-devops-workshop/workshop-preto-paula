package com.sifap.socialprogram.infrastructure;

import com.sifap.socialprogram.api.SocialProgramQueryPort;
import com.sifap.socialprogram.domain.ProgramStatus;
import com.sifap.socialprogram.domain.SocialProgram;
import com.sifap.socialprogram.infrastructure.persistence.SocialProgramRepository;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * JPA-backed adapter exposing the published port. Caching is layered
 * on top via {@code CachedSocialProgramAdapter} (T007).
 */
@Component
public class JpaSocialProgramQueryAdapter implements SocialProgramQueryPort {

    /** Bridge from the legacy numeric region code (1-26 + bypass 99) to UF letters. */
    static final String[] REGION_TO_UF = {
        null, "AC","AL","AM","AP","BA","CE","DF","ES","GO","MA","MG","MS","MT",
        "PA","PB","PE","PI","PR","RJ","RN","RO","RR","RS","SC","SE","SP","TO"
    };

    private final SocialProgramRepository repository;

    public JpaSocialProgramQueryAdapter(SocialProgramRepository repository) { this.repository = repository; }

    @Override
    public Optional<ProgramView> findActiveByCode(String programCode) {
        return repository.findByCode(programCode)
            .filter(p -> p.getStatus() != ProgramStatus.Retired)
            .map(p -> new ProgramView(
                p.getCode(),
                p.getType().name(),
                p.getBaseAmount(),
                p.getAdjustmentFactor(),
                null,                           // regional factor resolved on demand
                p.getStatus() == ProgramStatus.Active
            ));
    }

    @Override
    public BigDecimal regionalFactor(String programCode, short regionCode) {
        if (regionCode == 99) {
            // BR-024 / MYS-008 — region 99 bypasses eligibility but still pays. Use neutral factor.
            return BigDecimal.ONE;
        }
        if (regionCode < 1 || regionCode >= REGION_TO_UF.length) {
            throw new IllegalArgumentException("Unknown region code: " + regionCode);
        }
        String uf = REGION_TO_UF[regionCode];
        SocialProgram p = repository.findByCode(programCode)
            .orElseThrow(() -> new IllegalStateException("Program not found: " + programCode));
        return p.regionalFactor(uf)
            .orElseThrow(() -> new IllegalStateException(
                "No regional factor for program=%s uf=%s".formatted(programCode, uf)));
    }
}
