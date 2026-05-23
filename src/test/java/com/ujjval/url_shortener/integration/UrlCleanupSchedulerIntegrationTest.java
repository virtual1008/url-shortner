package com.ujjval.url_shortener.integration;

import com.ujjval.url_shortener.scheduler.UrlCleanupScheduler;
import com.ujjval.url_shortener.url.entity.UrlMapping;
import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "application.retention.deleted-days=30",
        "application.cleanup.cron=0 0 0 * * *"
})
@DisplayName("Scheduler - Cleanup Integration Test Suite")
@Transactional
public class UrlCleanupSchedulerIntegrationTest {

    @Autowired
    private UrlCleanupScheduler scheduler;

    @Autowired
    private UrlMappingRepository repository;

    @Test
    @DisplayName("Should remove URLs deleted longer than retention period")
    void shouldRemoveOldDeletedUrls() {
        // 1. Prepare data: URL deleted 40 days ago
        UrlMapping oldUrl = new UrlMapping();
        oldUrl.setShortCode("old123");
        oldUrl.setOriginalUrl("https://old.com");
        oldUrl.setDeletedAt(LocalDateTime.now().minusDays(40));
        repository.save(oldUrl);

        // 2. Prepare data: URL deleted 5 days ago (should stay)
        UrlMapping recentUrl = new UrlMapping();
        recentUrl.setShortCode("new123");
        recentUrl.setOriginalUrl("https://new.com");
        recentUrl.setDeletedAt(LocalDateTime.now().minusDays(5));
        repository.save(recentUrl);

        // 3. Execute cleanup
        scheduler.cleanupDeletedUrls();

        // 4. Verify
        assertFalse(repository.findByShortCode("old123").isPresent(), "Old URL should be purged");
        assertEquals(1, repository.count(), "Only the recent URL should remain");
    }
}