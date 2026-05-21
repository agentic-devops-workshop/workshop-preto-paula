package com.sifap.beneficiary.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

/** Beneficiary lifecycle (CL-002 in feature 002 spec). */
public enum LifecycleStatus {
    Active, Suspended, Cancelled, Inactive, Disabled;

    private static final Map<LifecycleStatus, Set<LifecycleStatus>> ALLOWED = Map.of(
        Active,    EnumSet.of(Suspended, Cancelled, Inactive),
        Suspended, EnumSet.of(Active, Cancelled, Disabled),
        Inactive,  EnumSet.of(Active),
        Cancelled, EnumSet.noneOf(LifecycleStatus.class),
        Disabled,  EnumSet.noneOf(LifecycleStatus.class)
    );

    public boolean canTransitionTo(LifecycleStatus target) {
        return ALLOWED.get(this).contains(target);
    }

    public boolean isTerminal() { return this == Cancelled || this == Disabled; }
}
