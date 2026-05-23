package com.ujjval.url_shortener.ratelimit.strategy;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * A pass-through strategy used strictly for baseline load testing and benchmarking.
 * This introduces zero latency and completely disables rate limiting.
 */
@Service
@ConditionalOnProperty(name = "application.ratelimit.type", havingValue = "none")
public class NoOpRateLimiterStrategy implements RateLimiterStrategy{
    @Override
    public boolean allowRequest(String identifier) {
        // Always allow the request. The door is wide open.
        return true;
    }

    @Override
    public void reset() {

    }
}
