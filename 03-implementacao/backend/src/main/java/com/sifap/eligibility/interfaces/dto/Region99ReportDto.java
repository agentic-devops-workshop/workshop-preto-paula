package com.sifap.eligibility.interfaces.dto;

import java.util.List;

public record Region99ReportDto(
    Long cycleId,
    String competence,
    String programCode,
    long totalBeneficiaries,
    long region99Count,
    double region99Percent,
    List<Region99BeneficiaryDto> beneficiaries
) {}