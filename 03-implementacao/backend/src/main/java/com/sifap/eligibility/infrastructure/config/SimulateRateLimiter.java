package com.sifap.eligibility.infrastructure.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulateRateLimiter {

    private final int perMinute;
    private final Clock clock;
    private final Map<String, Window> windows = new ConcurrentHashMap<>();

    public SimulateRateLimiter(@Value("${sifap.eligibility.simulate.rateLimit.perMinute:30}") int perMinute) {
        this(perMinute, Clock.systemUTC());
    }

    SimulateRateLimiter(int perMinute, Clock clock) {
        this.perMinute = perMinute;
        this.clock = clock;
    }

    public boolean tryAcquire(String subject) {
        String key = subject == null || subject.isBlank() ? "anonymous" : subject;
        long currentMinute = Instant.now(clock).getEpochSecond() / 60;
        Window window = windows.compute(key, (ignored, existing) -> {
            if (existing == null || existing.minute != currentMinute) {
                return new Window(currentMinute, 1);
            }
            return new Window(existing.minute, existing.count + 1);
        });
        return window.count <= perMinute;
    }

    private record Window(long minute, int count) {}
}