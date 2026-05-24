package com.ujjval.url_shortener.analytics.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class DeviceAnalyticsDto {
    private List<MetricCount> osBreakdown;
    private List<MetricCount> browserBreakdown;
}