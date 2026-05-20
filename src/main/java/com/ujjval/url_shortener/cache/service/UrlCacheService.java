package com.ujjval.url_shortener.cache.service;

public interface UrlCacheService {
    void save(String shortCode , String originalUrl);
    String get(String shortCode);
    void delete(String shortCode);
}
