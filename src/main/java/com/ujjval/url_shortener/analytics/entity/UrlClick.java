package com.ujjval.url_shortener.analytics.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name="url_clicks")
public class UrlClick {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code",nullable = false)
    private String shortCode;

    @Column(name="ip_address")
    private String ipAddress;

    @Column(name = "click_timestamp",nullable = false)
    private LocalDateTime clickTimestamp;

    @Column(name = "os")
    private String os;

    @Column(name = "browser")
    private String browser;

    @Column(name = "device_type")
    private String deviceType;

    @Column(name = "country")
    private String country;

    @Column(name = "city")
    private String city;

    public UrlClick(){}

    public UrlClick(String shortCode , String ipAddress , LocalDateTime clickTimestamp){
        this.shortCode = shortCode;
        this.ipAddress = ipAddress;
        this.clickTimestamp = clickTimestamp;
    }

    public Long getId(){
        return id;
    }
    public String getShortCode() {
        return shortCode;
    }
    public String getIpAddress() {
        return ipAddress;
    }
    public LocalDateTime getClickTimestamp() {
        return clickTimestamp;
    }
    public String getOs() { return os; }
    public void setOs(String os) { this.os = os; }

    public String getBrowser() { return browser; }
    public void setBrowser(String browser) { this.browser = browser; }

    public String getDeviceType() { return deviceType; }
    public void setDeviceType(String deviceType) { this.deviceType = deviceType; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }

    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
