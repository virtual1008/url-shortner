package com.ujjval.url_shortener.cache.service.impl;

import com.ujjval.url_shortener.cache.service.UrlCacheService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class UrlCacheServiceImpl implements UrlCacheService {
    private final StringRedisTemplate redisTemplate;

    @Value("${application.redis.url-prefix:url:}")
    private String urlCachePrefix;

    @Value("${application.redis.ttl-hours:24}")
    private long cacheTtlHours;
    @Override
    public void save(String shortCode, String originalUrl) {
          redisTemplate.opsForValue().set(
                  urlCachePrefix+shortCode,
                  originalUrl,
                  Duration.ofHours(cacheTtlHours)
          );
    }

    @Override
    public String get(String shortCode) {
        return redisTemplate.opsForValue().get(urlCachePrefix+shortCode);
    }

    @Override
    public void delete(String shortCode) {
         redisTemplate.delete(urlCachePrefix+shortCode);
    }
}
