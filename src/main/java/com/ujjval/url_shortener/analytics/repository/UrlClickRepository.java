package com.ujjval.url_shortener.analytics.repository;

import com.ujjval.url_shortener.analytics.dto.DailyClickStats;
import com.ujjval.url_shortener.analytics.dto.MetricCount;
import com.ujjval.url_shortener.analytics.entity.UrlClick;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface UrlClickRepository extends JpaRepository<UrlClick,Long> {
    long countByShortCode(String shortCode);

    @Query("SELECT COUNT(DISTINCT c.ipAddress) FROM UrlClick c WHERE c.shortCode = :shortCode")
    long countUniqueVisitors(@Param("shortCode") String shortCode);

    @Query("SELECT MAX(c.clickTimestamp) FROM UrlClick c WHERE c.shortCode = :shortCode")
    LocalDateTime getLastClickTime(@Param("shortCode") String shortCode);

    @Query(value = """
            SELECT CAST(click_timestamp AS DATE) as clickDate, COUNT(*) as clickCount 
            FROM url_clicks 
            WHERE short_code = :shortCode AND click_timestamp >= :startDate 
            GROUP BY CAST(click_timestamp AS DATE) 
            ORDER BY clickDate ASC
            """, nativeQuery = true)
    List<DailyClickStats> getDailyClickStats(@Param("shortCode") String shortCode, @Param("startDate") LocalDateTime startDate);

    @Query("SELECT c.os as metric, COUNT(c) as count FROM UrlClick c WHERE c.shortCode = :shortCode AND c.os IS NOT NULL GROUP BY c.os ORDER BY count DESC")
    List<MetricCount> getOsBreakdown(@Param("shortCode") String shortCode);

    @Query("SELECT c.browser as metric, COUNT(c) as count FROM UrlClick c WHERE c.shortCode = :shortCode AND c.browser IS NOT NULL GROUP BY c.browser ORDER BY count DESC")
    List<MetricCount> getBrowserBreakdown(@Param("shortCode") String shortCode);

    @Query("SELECT c.country as metric, COUNT(c) as count FROM UrlClick c WHERE c.shortCode = :shortCode AND c.country IS NOT NULL GROUP BY c.country ORDER BY count DESC")
    List<MetricCount> getCountryBreakdown(@Param("shortCode") String shortCode);
}
