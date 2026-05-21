package com.sifap.eligibility.application;

import com.sifap.eligibility.api.BeneficiaryEligibilityPort;
import com.sifap.eligibility.api.EligibilityResult;
import com.sifap.eligibility.interfaces.dto.Region99BeneficiaryDto;
import com.sifap.eligibility.interfaces.dto.Region99ReportDto;
import com.sifap.eligibility.interfaces.dto.SimulateRequest;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

@Service
public class EligibilityService {

    private static final int DEFAULT_LIMIT = 1_000;

    private final BeneficiaryEligibilityPort eligibilityPort;
    private final JdbcOperations jdbcOperations;
    private final ApplicationEventPublisher events;

    public EligibilityService(BeneficiaryEligibilityPort eligibilityPort,
                              JdbcOperations jdbcOperations,
                              ApplicationEventPublisher events) {
        this.eligibilityPort = eligibilityPort;
        this.jdbcOperations = jdbcOperations;
        this.events = events;
    }

    public EligibilityResult simulate(SimulateRequest request) {
        return eligibilityPort.validate(
            request.programCode(),
            request.birthDate(),
            request.familyIncome(),
            request.dependents(),
            request.regionCode()
        );
    }

    public Region99ReportDto region99Report(Long cycleId,
                                            String competence,
                                            String programCode,
                                            boolean revealCpf) {
        validateReportFilters(cycleId, competence, programCode);

        List<Region99BeneficiaryDto> beneficiaries = cycleId != null
            ? reportByCycle(cycleId, revealCpf)
            : reportByCompetence(programCode, revealCpf);

        if (revealCpf) {
            beneficiaries.forEach(row -> events.publishEvent(new CpfRevealed(
                row.cpf(), cycleId, competence, programCode, OffsetDateTime.now())));
        }

        long region99Count = beneficiaries.size();
        long totalBeneficiaries = Math.max(region99Count, 1L);
        double percent = region99Count * 100.0 / totalBeneficiaries;

        return new Region99ReportDto(cycleId, competence, programCode,
            totalBeneficiaries, region99Count, percent, beneficiaries);
    }

    private static void validateReportFilters(Long cycleId, String competence, String programCode) {
        boolean byCycle = cycleId != null;
        boolean byCompetence = competence != null && !competence.isBlank()
            && programCode != null && !programCode.isBlank();
        if (byCycle == byCompetence) {
            throw new IllegalArgumentException("Provide exactly one filter mode: cycleId OR competence + programCode");
        }
    }

    private List<Region99BeneficiaryDto> reportByCycle(Long cycleId, boolean revealCpf) {
        return jdbcOperations.query(
            """
                        select p.cpf, b.birth_date, p.generated_at as last_update
                            from payment p
                            join beneficiary b on b.id = p.beneficiary_id
                         where p.cycle_id = ?
                             and p.region_bypass = true
                         order by p.cpf
             limit ?
            """,
            (rs, rowNum) -> Region99BeneficiaryDto.from(
                rs.getString("cpf"),
                calculateAge(rs.getObject("birth_date", LocalDate.class), LocalDate.now()),
                                rs.getTimestamp("last_update").toLocalDateTime().toLocalDate(),
                revealCpf
            ),
            cycleId,
            DEFAULT_LIMIT
        );
    }

    private List<Region99BeneficiaryDto> reportByCompetence(String programCode, boolean revealCpf) {
        return jdbcOperations.query(
            """
                        select cpf, birth_date, updated_at as last_update
              from beneficiary
             where region_code = 99
               and program_code = ?
             order by cpf
             limit ?
            """,
            (rs, rowNum) -> Region99BeneficiaryDto.from(
                rs.getString("cpf"),
                calculateAge(rs.getObject("birth_date", LocalDate.class), LocalDate.now()),
                rs.getTimestamp("last_update").toLocalDateTime().toLocalDate(),
                revealCpf
            ),
            programCode,
            DEFAULT_LIMIT
        );
    }

    private static int calculateAge(LocalDate birthDate, LocalDate referenceDate) {
        if (birthDate == null) return 0;
        return java.time.Period.between(birthDate, referenceDate).getYears();
    }

    public record CpfRevealed(String cpf, Long cycleId, String competence, String programCode, OffsetDateTime occurredAt) {}
}