package com.ujjval.url_shortener.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class AnalyticsSummaryDto {
    private String shortCode;
    private long totalClicks;
    private long uniqueVisitors;
    private LocalDateTime lastVisitedAt;
}
