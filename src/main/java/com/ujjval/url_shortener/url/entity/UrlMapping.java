package com.ujjval.url_shortener.url.entity;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "url_mappings")
public class UrlMapping {
    @Id
    //@GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "short_code",unique = true)
    private String shortCode;

    @Column(name = "original_url",nullable = false ,columnDefinition = "TEXT")
    private String originalUrl;

    @Column(name = "created_at",nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at")
    private LocalDateTime expiresAt;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    public UrlMapping(){
    }
    @PrePersist
    public void prePersist(){
        this.createdAt = LocalDateTime.now();
    }

    public void setId(long id){
        this.id = id;
    }
    public Long getId(){
        return id;
    }
    public String getShortCode(){
        return shortCode;
    }

    public void setShortCode(String shortCode){
        this.shortCode = shortCode;
    }
    public String getOriginalUrl(){
        return  originalUrl;
    }
    public void setOriginalUrl(String originalUrl){
        this.originalUrl = originalUrl;
    }
    public LocalDateTime getCreatedAt(){
        return createdAt;
    }
    public LocalDateTime getExpiresAt(){
        return  expiresAt;
    }
    public void setExpiresAt(LocalDateTime expiresAt){
        this.expiresAt = expiresAt;
    }
    public LocalDateTime getDeletedAt(){
        return deletedAt;
    }
    public void setDeletedAt(LocalDateTime deletedAt){
        this.deletedAt = deletedAt;
    }
}
