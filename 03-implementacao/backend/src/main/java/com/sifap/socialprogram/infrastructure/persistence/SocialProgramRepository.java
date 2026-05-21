package com.sifap.socialprogram.infrastructure.persistence;

import com.sifap.socialprogram.domain.SocialProgram;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SocialProgramRepository extends JpaRepository<SocialProgram, Long> {
    Optional<SocialProgram> findByCode(String code);
    boolean existsByCode(String code);
}
