package com.sifap.beneficiary.domain;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LifecycleStatusTest {

    @Test
    void active_can_transition_to_suspended_cancelled_inactive() {
        assertThat(LifecycleStatus.Active.canTransitionTo(LifecycleStatus.Suspended)).isTrue();
        assertThat(LifecycleStatus.Active.canTransitionTo(LifecycleStatus.Cancelled)).isTrue();
        assertThat(LifecycleStatus.Active.canTransitionTo(LifecycleStatus.Inactive)).isTrue();
    }

    @Test
    void suspended_can_reactivate_or_terminate() {
        assertThat(LifecycleStatus.Suspended.canTransitionTo(LifecycleStatus.Active)).isTrue();
        assertThat(LifecycleStatus.Suspended.canTransitionTo(LifecycleStatus.Cancelled)).isTrue();
        assertThat(LifecycleStatus.Suspended.canTransitionTo(LifecycleStatus.Disabled)).isTrue();
    }

    @Test
    void cancelled_is_terminal_per_CL_002() {
        for (LifecycleStatus target : LifecycleStatus.values()) {
            assertThat(LifecycleStatus.Cancelled.canTransitionTo(target))
                .as("Cancelled → %s must be forbidden", target)
                .isFalse();
        }
        assertThat(LifecycleStatus.Cancelled.isTerminal()).isTrue();
    }

    @Test
    void disabled_is_terminal() {
        for (LifecycleStatus target : LifecycleStatus.values()) {
            assertThat(LifecycleStatus.Disabled.canTransitionTo(target)).isFalse();
        }
        assertThat(LifecycleStatus.Disabled.isTerminal()).isTrue();
    }
}
