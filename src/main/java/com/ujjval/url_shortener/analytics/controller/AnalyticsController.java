package com.ujjval.url_shortener.analytics.controller;


import com.ujjval.url_shortener.analytics.dto.*;
import com.ujjval.url_shortener.analytics.repository.UrlClickRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/v1/analytics")
@RequiredArgsConstructor
@Slf4j
public class AnalyticsController {
    private final UrlClickRepository urlClickRepository;

    @GetMapping("/{shortCode}")
    public ResponseEntity<AnalyticsResponseDto> getAnalytics(@PathVariable String shortCode) {
        long totalClicks = urlClickRepository.countByShortCode(shortCode);
        return ResponseEntity.ok(new AnalyticsResponseDto(shortCode,totalClicks));
    }

    @GetMapping("/{shortCode}/summary")
    public ResponseEntity<AnalyticsSummaryDto> getAnalyticsSummary(@PathVariable String shortCode) {
        log.debug("Fetching analytics summary for: {}", shortCode);

        long totalClicks = urlClickRepository.countByShortCode(shortCode);
        long uniqueVisitors = urlClickRepository.countUniqueVisitors(shortCode);
        LocalDateTime lastClick = urlClickRepository.getLastClickTime(shortCode);

        AnalyticsSummaryDto summary = AnalyticsSummaryDto.builder()
                .shortCode(shortCode)
                .totalClicks(totalClicks)
                .uniqueVisitors(uniqueVisitors)
                .lastVisitedAt(lastClick)
                .build();

        return ResponseEntity.ok(summary);
    }

    @GetMapping("/{shortCode}/timeseries")
    public ResponseEntity<List<DailyClickStats>> getTimeSeriesData(
            @PathVariable String shortCode,
            @RequestParam(defaultValue = "7") int days) {

        log.debug("Fetching {}-day time-series data for: {}", days, shortCode);

        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        List<DailyClickStats> graphData = urlClickRepository.getDailyClickStats(shortCode, startDate);

        return ResponseEntity.ok(graphData);
    }

    // --- FUTURE ENDPOINTS (Stubs) --- //

    @GetMapping("/{shortCode}/devices")
    public ResponseEntity<?> getDeviceStats(@PathVariable String shortCode) {
        log.debug("Fetching device analytics for: {}", shortCode);

        List<MetricCount> osStats = urlClickRepository.getOsBreakdown(shortCode);
        List<MetricCount> browserStats = urlClickRepository.getBrowserBreakdown(shortCode);

        return ResponseEntity.ok(new DeviceAnalyticsDto(osStats, browserStats));
    }

    @GetMapping("/{shortCode}/locations")
    public ResponseEntity<?> getLocationStats(@PathVariable String shortCode) {
        log.debug("Fetching location analytics for: {}", shortCode);
        List<MetricCount> countryStats = urlClickRepository.getCountryBreakdown(shortCode);
        return ResponseEntity.ok(countryStats);
    }
}
