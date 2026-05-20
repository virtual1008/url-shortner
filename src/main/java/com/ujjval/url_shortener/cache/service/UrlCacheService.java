package com.ujjval.url_shortener.cache.service;

import java.time.LocalDateTime;

public interface UrlCacheService {
    void save(String shortCode, String originalUrl, LocalDateTime expiresAt);
    String get(String shortCode);
    void delete(String shortCode);
}
