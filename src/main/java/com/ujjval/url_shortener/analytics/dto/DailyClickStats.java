package com.ujjval.url_shortener.analytics.dto;

import java.time.LocalDate;

public interface DailyClickStats {
    LocalDate getClickDate();
    Long getClickCount();
}
