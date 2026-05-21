package com.sifap.eligibility.infrastructure.config;

import org.junit.jupiter.api.Test;
import org.springframework.jdbc.core.JdbcOperations;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class Region99StartupGuardTest {

    private final JdbcOperations jdbcOperations = mock(JdbcOperations.class);

    @Test
    void verifyRegion99RowsWhenBypassDisabled_should_fail_when_legacy_rows_exist() {
        when(jdbcOperations.queryForObject("select count(*) from beneficiary where region_code = 99", Long.class))
            .thenReturn(1L);

        Region99StartupGuard guard = new Region99StartupGuard(jdbcOperations, false);

        assertThatThrownBy(guard::verifyRegion99RowsWhenBypassDisabled)
            .isInstanceOf(IllegalStateException.class)
            .hasMessageContaining("regionCode=99 rows still exist");
    }

    @Test
    void verifyRegion99RowsWhenBypassDisabled_should_skip_probe_when_bypass_enabled() {
        Region99StartupGuard guard = new Region99StartupGuard(jdbcOperations, true);

        assertThatCode(guard::verifyRegion99RowsWhenBypassDisabled).doesNotThrowAnyException();
        verify(jdbcOperations, never()).queryForObject("select count(*) from beneficiary where region_code = 99", Long.class);
    }
}