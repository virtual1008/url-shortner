package com.ujjval.url_shortener.analytics.strategy;

public interface ClickTrackingStrategy {
    void trackClick(String shortCode , String clientIp,String userAgent);
}
