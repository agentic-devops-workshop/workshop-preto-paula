package com.sifap.beneficiary.interfaces.dto;

import com.sifap.beneficiary.domain.LifecycleStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ChangeLifecycleStatusRequest(
    @NotNull  LifecycleStatus target,
    @NotBlank String reason
) {}
