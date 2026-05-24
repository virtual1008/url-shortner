package com.ujjval.url_shortener.analytics.dto;

import java.time.LocalDateTime;

public class ClickEventDto {
    private String shortCode;
    private String ipAddress;
    private LocalDateTime timestamp;
    private String userAgent; // The new field

    public ClickEventDto(){}

    // 1. Updated Constructor
    public ClickEventDto(String shortCode, String ipAddress, LocalDateTime timestamp, String userAgent){
        this.shortCode = shortCode;
        this.ipAddress = ipAddress;
        this.timestamp = timestamp;
        this.userAgent = userAgent;
    }

    public String getShortCode(){
        return shortCode;
    }

    public void setShortCode(String shortCode){
        this.shortCode = shortCode;
    }

    public String getIpAddress(){
        return ipAddress;
    }

    public void setIpAddress(String ipAddress){
        this.ipAddress = ipAddress;
    }

    public LocalDateTime getTimestamp(){
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp){
        this.timestamp = timestamp;
    }

    // 2. Added Getter for UserAgent
    public String getUserAgent() {
        return userAgent;
    }

    // 3. Added Setter for UserAgent
    public void setUserAgent(String userAgent) {
        this.userAgent = userAgent;
    }
}