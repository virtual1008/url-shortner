package com.ujjval.url_shortener.scheduler;

import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import static org.junit.jupiter.api.Assertions.*;


@DisplayName("Scheduler Module - Unit Test Suite")
public class UrlCleanupSchedulerTest {
    @Mock
    private UrlMappingRepository repository;

    @InjectMocks
    private UrlCleanupScheduler scheduler;

    @Captor
    private ArgumentCaptor<LocalDateTime> dateCaptor;

    @BeforeEach
    void setUp(){
        MockitoAnnotations.openMocks(this);
        ReflectionTestUtils.setField(scheduler,"retentionDays",30L);
    }


    @Nested
    @DisplayName("Cleanup Execution Tests")
    class CleanupExecutionTests {
        @Test
        @DisplayName(
                """
                Test Case 1:
                Should calculate correct cutoff date and trigger deletion
        
                Expected Result:
                Repository receives a cutoff date exactly 30 days in the past
                """
        )
        void shouldExecuteCleanupSuccessfully() {
            when(repository.deleteByDeletedAtBefore(any(LocalDateTime.class))).thenReturn(5);

            scheduler.cleanupDeletedUrls();
            verify(repository, times(1)).deleteByDeletedAtBefore(dateCaptor.capture());
            LocalDateTime capturedDate = dateCaptor.getValue();

            LocalDateTime expectedDate = LocalDateTime.now().minusDays(30);
            long differenceInSeconds = Math.abs(ChronoUnit.SECONDS.between(capturedDate, expectedDate));
            assertTrue(differenceInSeconds <= 1,
                    "Calculated cutoff date should be exactly 30 days ago (within 1 second)");
        }

        @Test
        @DisplayName(
                """
                Test Case 2:
                Should execute smoothly even if no URLs are ready for deletion
        
                Expected Result:
                Repository returns 0 deletions, no exceptions are thrown
                """
        )
        void shouldHandleZeroDeletionsSmoothly() {
            when(repository.deleteByDeletedAtBefore(any(LocalDateTime.class))).thenReturn(0);
            scheduler.cleanupDeletedUrls();
            verify(repository, times(1)).deleteByDeletedAtBefore(any(LocalDateTime.class));
        }

    }
}
