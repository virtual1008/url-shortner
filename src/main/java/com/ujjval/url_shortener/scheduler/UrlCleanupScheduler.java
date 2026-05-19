package com.ujjval.url_shortener.scheduler;

import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Slf4j
@Component
@RequiredArgsConstructor
public class UrlCleanupScheduler {

    private final UrlMappingRepository repository;

    @Scheduled(cron = "${application.cleanup.cron}")
    public void cleanupDeletedUrls(){
        LocalDateTime now = LocalDateTime.now();
        int deletedCount = repository.deleteByDeletedAtBefore(now);
        log.info("Deleted {} expired URLs at {}", deletedCount, now);
    }
}
