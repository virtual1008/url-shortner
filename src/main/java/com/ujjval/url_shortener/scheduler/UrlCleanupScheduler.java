package com.ujjval.url_shortener.scheduler;

import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlCleanupScheduler {

    private final UrlMappingRepository repository;

    @Value("${application.retention.deleted-days}")
    private long retentionDays;

    @Scheduled(cron = "${application.cleanup.cron}")
    public void cleanupDeletedUrls(){
        LocalDateTime cutoff = LocalDateTime.now().minusDays(retentionDays);
        int deletedCount = repository.deleteByDeletedAtBefore(cutoff);
        log.info("Deleted {} expired URLs at {}", deletedCount, cutoff);
    }
}
