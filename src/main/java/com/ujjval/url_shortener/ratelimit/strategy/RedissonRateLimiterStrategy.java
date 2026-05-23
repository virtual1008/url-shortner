package com.ujjval.url_shortener.ratelimit.strategy;

import lombok.RequiredArgsConstructor;
import org.redisson.api.RRateLimiter;
import org.redisson.api.RateIntervalUnit;
import org.redisson.api.RateType;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;

/**
 * A distributed Rate Limiting Strategy utilizing Redis via Redisson.
 * * WHY IN-MEMORY FAILS IN DISTRIBUTED SYSTEMS:
 * In a horizontally scaled environment with multiple active instances (pods), an in-memory
 * rate limiter is highly vulnerable to the "Split-Brain" problem. Because a Load Balancer
 * distributes incoming traffic across N instances, an in-memory strategy effectively multiplies
 * the rate limit by N. (e.g., A limit of 5 req/sec across 4 instances allows a user 20 req/sec
 * before total lockout, completely bypassing the intended threshold).
 * * THE REDIS SOLUTION:
 * This strategy centralizes the token bucket state in a shared Redis cluster. When any single
 * server instance consumes a token, the updated state is instantly visible to all other instances.
 * This ensures strict, global rate limit enforcement regardless of how the Load Balancer routes the traffic.
 */
@Service
@ConditionalOnProperty(name = "application.ratelimit.type", havingValue = "redis")
@RequiredArgsConstructor
public class RedissonRateLimiterStrategy implements RateLimiterStrategy{
    private final RedissonClient redissonClient;

    @Value("${application.ratelimit.key-prefix:rate_limit:ip:}")
    private String redisKeyPrefix;

    @Override
    public boolean allowRequest(String identifier) {
        // 1. Create a globally unique Redis key for this IP address
        String redisKey = redisKeyPrefix + identifier;

        // 2. Fetch the RateLimiter object from the shared Redis cluster
        RRateLimiter rateLimiter = redissonClient.getRateLimiter(redisKey);

        // 3. Initialize the bucket if it does not exist.
        // RateType.OVERALL ensures all instances share this exact limit.
        // Rule: 5 requests per 1 second.
        rateLimiter.trySetRate(RateType.OVERALL,5, 1, RateIntervalUnit.SECONDS);
        // 4. Consume 1 token. Returns true if successful, false if the bucket is empty.
        return rateLimiter.tryAcquire(1);
    }

    @Override
    public void reset() {

        // You need to know the identifiers to delete specific keys,
        // OR you can clear the entire rate limiter state if your implementation allows.

        // Since reset() in a test usually implies clearing a specific identifier
        // or wiping the namespace, here is the standard approach:

        // If you want to clear a specific key, you'd need the identifier.
        // For testing purposes, if you want to wipe all rate limit keys:
        redissonClient.getKeys().deleteByPattern(redisKeyPrefix + "*");
    }
}
