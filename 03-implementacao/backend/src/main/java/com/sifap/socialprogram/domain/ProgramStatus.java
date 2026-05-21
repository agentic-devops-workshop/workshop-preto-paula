package com.sifap.socialprogram.domain;

import java.util.EnumSet;
import java.util.Map;
import java.util.Set;

public enum ProgramStatus {
    Active, Suspended, Retired;

    private static final Map<ProgramStatus, Set<ProgramStatus>> ALLOWED = Map.of(
        Active,    EnumSet.of(Suspended, Retired),
        Suspended, EnumSet.of(Active, Retired),
        Retired,   EnumSet.noneOf(ProgramStatus.class)        // terminal
    );

    public boolean canTransitionTo(ProgramStatus target) {
        return ALLOWED.get(this).contains(target);
    }

    public boolean isTerminal() { return this == Retired; }
}
