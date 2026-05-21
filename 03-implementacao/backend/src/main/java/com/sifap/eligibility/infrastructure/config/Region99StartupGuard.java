package com.sifap.eligibility.infrastructure.config;

import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.stereotype.Component;

@Component
@Profile("prod")
public class Region99StartupGuard {

    private final JdbcOperations jdbcOperations;
    private final boolean bypassEnabled;

    public Region99StartupGuard(JdbcOperations jdbcOperations,
                                @Value("${sifap.eligibility.region99Bypass.enabled:true}") boolean bypassEnabled) {
        this.jdbcOperations = jdbcOperations;
        this.bypassEnabled = bypassEnabled;
    }

    @PostConstruct
    void verifyRegion99RowsWhenBypassDisabled() {
        if (bypassEnabled) {
            return;
        }
        Long count = jdbcOperations.queryForObject(
            "select count(*) from beneficiary where region_code = 99",
            Long.class
        );
        if (count != null && count > 0) {
            throw new IllegalStateException(
                "Region-99 bypass is disabled but legacy regionCode=99 rows still exist: " + count);
        }
    }
}