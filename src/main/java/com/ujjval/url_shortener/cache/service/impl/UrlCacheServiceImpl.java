package com.ujjval.url_shortener.cache.service.impl;

import com.ujjval.url_shortener.cache.service.UrlCacheService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class UrlCacheServiceImpl implements UrlCacheService {
    private final StringRedisTemplate redisTemplate;

    @Value("${application.redis.url-prefix:url:}")
    private String urlCachePrefix;

    @Value("${application.redis.ttl-hours:24}")
    private long cacheTtlHours;
    @Override
    public void save(String shortCode, String originalUrl, LocalDateTime expiresAt) {
        String key = urlCachePrefix + shortCode;
        try {
            log.debug("Attempting to cache original URL for shortCode: {}", shortCode);
            Duration ttl = Duration.ofHours(cacheTtlHours);
            if (expiresAt != null) {
                Duration timeUntilExpiry = Duration.between(LocalDateTime.now(), expiresAt);
                if (timeUntilExpiry.compareTo(ttl) < 0) {
                    ttl = timeUntilExpiry;
                }
                if (ttl.isNegative() || ttl.isZero()) {
                    log.debug("Skipping cache for shortCode: {} because it is already expired", shortCode);
                    return;
                }
            }
            redisTemplate.opsForValue().set(key, originalUrl, ttl);
            log.debug("Successfully cached original URL for shortCode: {} with dynamic TTL: {} seconds", shortCode, ttl.getSeconds());

        } catch (Exception e) {
            log.error("Redis Write Failure for shortCode: {}. Exception: {}", shortCode, e.getMessage(), e);
        }
    }
    @Override
    public String get(String shortCode) {
        String key = urlCachePrefix + shortCode;
        try {
            log.debug("Fetching original URL from cache for shortCode: {}", shortCode);

            String originalUrl = redisTemplate.opsForValue().get(key);

            if (originalUrl != null) {
                log.debug("Cache HIT for shortCode: {}", shortCode);
            } else {
                log.debug("Cache MISS for shortCode: {}", shortCode);
            }
            return originalUrl;

        } catch (Exception e) {
            // Log the failure and return null (simulating a cache miss).
            // This forces the app to gracefully fallback to the primary Database.
            log.error("Redis Read Failure: Could not fetch cache for shortCode: {}. Exception: {}", shortCode, e.getMessage(), e);
            return null;
        }
    }

    @Override
    public void delete(String shortCode) {
        String key = urlCachePrefix + shortCode;
        try {
            log.debug("Attempting to evict cache for shortCode: {}", shortCode);

            Boolean deleted = redisTemplate.delete(key);

            if (Boolean.TRUE.equals(deleted)) {
                log.debug("Successfully evicted cache for shortCode: {}", shortCode);
            } else {
                log.debug("Cache eviction skipped - key not found for shortCode: {}", shortCode);
            }
        } catch (Exception e) {
            log.error("Redis Delete Failure: Could not delete cache for shortCode: {}. Exception: {}", shortCode, e.getMessage(), e);
        }
    }
}