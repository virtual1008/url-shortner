package com.ujjval.url_shortener.url.repository;

import com.ujjval.url_shortener.url.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping,Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
    Optional<UrlMapping> findByShortCodeAndDeletedAtIsNull(String shortCode);
    @Modifying
    @Transactional
    int deleteByDeletedAtBefore(LocalDateTime time);
}
