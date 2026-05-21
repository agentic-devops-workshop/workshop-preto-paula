package com.sifap.socialprogram.application;

import com.sifap.socialprogram.domain.ProgramStatus;
import com.sifap.socialprogram.domain.ProgramType;
import com.sifap.socialprogram.domain.SocialProgram;
import com.sifap.socialprogram.infrastructure.persistence.SocialProgramRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Map;

@Service
@Transactional
public class SocialProgramService {

    private final SocialProgramRepository repository;

    public SocialProgramService(SocialProgramRepository repository) { this.repository = repository; }

    public SocialProgram create(String code, ProgramType type, BigDecimal baseAmount,
                                BigDecimal adjustmentFactor, LocalDate effectiveFrom) {
        if (repository.existsByCode(code)) {
            throw new ProgramAlreadyExistsException(code);
        }
        try {
            SocialProgram p = new SocialProgram(code, type, baseAmount, adjustmentFactor, effectiveFrom);
            return repository.saveAndFlush(p);
        } catch (DataIntegrityViolationException ex) {
            throw new ProgramAlreadyExistsException(code, ex);
        }
    }

    @Transactional(readOnly = true)
    public SocialProgram findByCode(String code) {
        return repository.findByCode(code).orElseThrow(() -> new ProgramNotFoundException(code));
    }

    public SocialProgram updateAdjustmentFactor(String code, BigDecimal newFactor) {
        SocialProgram p = findByCode(code);
        p.changeAdjustmentFactor(newFactor);
        return p;
    }

    public SocialProgram replaceRegionalFactors(String code, Map<String, BigDecimal> ufToFactor) {
        SocialProgram p = findByCode(code);
        p.replaceRegionalFactors(ufToFactor);
        return p;
    }

    public SocialProgram changeStatus(String code, ProgramStatus target) {
        SocialProgram p = findByCode(code);
        p.changeStatus(target);
        return p;
    }

    public static class ProgramAlreadyExistsException extends RuntimeException {
        public ProgramAlreadyExistsException(String code) { super("Program " + code + " already exists"); }
        public ProgramAlreadyExistsException(String code, Throwable cause) { super("Program " + code + " already exists", cause); }
    }

    public static class ProgramNotFoundException extends RuntimeException {
        public ProgramNotFoundException(String code) { super("Program " + code + " not found"); }
    }
}
