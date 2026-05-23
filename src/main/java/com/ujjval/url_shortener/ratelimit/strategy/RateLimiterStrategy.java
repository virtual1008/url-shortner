package com.ujjval.url_shortener.ratelimit.strategy;

public interface RateLimiterStrategy {

    /**
     * Determines if a request should be allowed based on the implemented algorithm.
     * @param identifier The unique key (e.g., IP Address)
     * @return true if allowed, false if HTTP 429 should be thrown
     */
    boolean allowRequest(String identifier);
    void reset();
}
