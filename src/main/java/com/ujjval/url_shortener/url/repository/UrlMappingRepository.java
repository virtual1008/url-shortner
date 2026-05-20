package com.ujjval.url_shortener.url.repository;

import com.ujjval.url_shortener.url.entity.UrlMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Optional;

public interface UrlMappingRepository extends JpaRepository<UrlMapping,Long> {
    Optional<UrlMapping> findByShortCode(String shortCode);
    Optional<UrlMapping> findByShortCodeAndDeletedAtIsNull(String shortCode);

    boolean existsByShortCode(String shortCode);
    @Modifying
    @Transactional
    int deleteByDeletedAtBefore(LocalDateTime time);

    @Modifying
    @Transactional
    @Query("UPDATE UrlMapping u SET u.clickCount = u.clickCount + 1 WHERE u.shortCode = :shortCode")
    void incrementClickCount(@Param("shortCode") String shortCode);

    Page<UrlMapping> findByDeletedAtNotNull(Pageable pageable);
}
