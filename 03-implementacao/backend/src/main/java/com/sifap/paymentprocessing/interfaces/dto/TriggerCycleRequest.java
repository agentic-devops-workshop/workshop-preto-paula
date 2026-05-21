package com.sifap.paymentprocessing.interfaces.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record TriggerCycleRequest(
    @NotBlank @Pattern(regexp = "^[0-9]{4}-(0[1-9]|1[0-2])$") String competence,
    @NotBlank @Size(min = 4, max = 4)                          String programCode,
    boolean forceBackfill,
    String backfillReason
) {}
