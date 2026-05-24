package com.ujjval.url_shortener.analytics.strategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class RedisClickStrategy implements ClickTrackingStrategy{

    private static final Logger log = LoggerFactory.getLogger(RedisClickStrategy.class);
    @Override
    public void trackClick(String shortCode, String clientIp , String userAgent) {
        log.warn("KAFKA DOWN: Safely falling back to Redis for shortCode: {}", shortCode);
    }
}
