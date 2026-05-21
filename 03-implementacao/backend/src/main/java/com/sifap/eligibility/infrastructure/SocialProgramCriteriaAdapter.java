package com.sifap.eligibility.infrastructure;

import com.sifap.eligibility.application.ports.ProgramCriteriaPort;
import com.sifap.eligibility.domain.EligibilityCriteria;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Optional;

/**
 * Maps SocialProgramRegistry persistence columns to the local eligibility shape.
 *
 * <p>Finding F2 from the analyze report found that feature 003's published
 * {@code SocialProgramQueryPort.ProgramView} intentionally omits eligibility
 * thresholds. Until that port grows a backward-compatible eligibility view,
 * this adapter reads the feature-003-owned {@code social_program} table using
 * parameterized SQL and returns the local {@link EligibilityCriteria} record.
 */
@Component
public class SocialProgramCriteriaAdapter implements ProgramCriteriaPort {

    private final JdbcTemplate jdbcTemplate;

    public SocialProgramCriteriaAdapter(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    public Optional<EligibilityCriteria> findByCode(String programCode) {
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(
                """
                select code, type, income_max, status
                  from social_program
                 where code = ?
                """,
                (rs, rowNum) -> new EligibilityCriteria(
                    rs.getString("code"),
                    rs.getString("type").charAt(0),
                    rs.getObject("income_max", BigDecimal.class),
                    "Active".equalsIgnoreCase(rs.getString("status")),
                    "Retired".equalsIgnoreCase(rs.getString("status"))
                ),
                programCode
            ));
        } catch (EmptyResultDataAccessException ex) {
            return Optional.empty();
        }
    }
}