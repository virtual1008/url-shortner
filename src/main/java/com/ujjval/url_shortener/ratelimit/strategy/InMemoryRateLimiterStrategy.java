package com.ujjval.url_shortener.ratelimit.strategy;

import com.ujjval.url_shortener.ratelimit.TokenBucket;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;

@Service
@ConditionalOnProperty(name = "application.ratelimit.type", havingValue = "in-memory", matchIfMissing = true)
public class InMemoryRateLimiterStrategy implements RateLimiterStrategy{
    private final ConcurrentHashMap<String, TokenBucket> limiters = new ConcurrentHashMap<>();
    private final long MAX_CAPACITY = 5;
    private final double REFILL_RATE = 1.0;
    @Override
    public boolean allowRequest(String identifier) {
        TokenBucket bucket = limiters.computeIfAbsent(
                identifier,
                key -> new TokenBucket(MAX_CAPACITY, REFILL_RATE)
        );
        return bucket.tryConsume();
    }

    @Override
    public void reset() {
        limiters.clear();
    }

}
