package com.sifap.eligibility.infrastructure.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class SimulateRateLimiter {

    private final int perMinute;
    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    public SimulateRateLimiter(@Value("${sifap.eligibility.simulate.rateLimit.perMinute:30}") int perMinute) {
        this.perMinute = perMinute;
    }

    public boolean tryAcquire(String subject) {
        String key = subject == null || subject.isBlank() ? "anonymous" : subject;
        return buckets.computeIfAbsent(key, ignored -> newBucket()).tryConsume(1);
    }

    private Bucket newBucket() {
        Bandwidth limit = Bandwidth.classic(perMinute, Refill.greedy(perMinute, Duration.ofMinutes(1)));
        return Bucket.builder().addLimit(limit).build();
    }
}