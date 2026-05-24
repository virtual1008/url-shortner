package com.ujjval.url_shortener.scheduler;

import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@DisplayName("Scheduler Module - Unit Test Suite")
public class UrlCleanupSchedulerTest {

    // @Mock creates a fake, empty shell of the repository.
    // It will not connect to a database; it only does what we tell it to do.
    @Mock
    private UrlMappingRepository repository;

    // @InjectMocks creates a real instance of our Scheduler,
    // but forces the fake @Mock repository inside of it.
    @InjectMocks
    private UrlCleanupScheduler scheduler;

    // ArgumentCaptor allows us to "intercept" the exact date the scheduler
    // passes into the repository so we can inspect it during the test.
    @Captor
    private ArgumentCaptor<LocalDateTime> dateCaptor;

    @BeforeEach
    void setUp() {
        // Initialize all Mockito annotations (@Mock, @InjectMocks, @Captor)
        MockitoAnnotations.openMocks(this);

        // Because we are not using Spring Boot (@SpringBootTest), Spring won't
        // read our application.yml file. We use ReflectionTestUtils to manually
        // inject the @Value("${application.retention.deleted-days}") field.
        ReflectionTestUtils.setField(scheduler, "retentionDays", 30L);
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
            // Arrange: Tell the fake repository to pretend it deleted 5 records
            when(repository.deleteByDeletedAtBefore(any(LocalDateTime.class))).thenReturn(5);

            // Act: Run the actual business logic
            scheduler.cleanupDeletedUrls();

            // Assert 1: Verify the repository's delete method was called exactly once,
            // and capture the LocalDateTime object that was passed into it.
            verify(repository, times(1)).deleteByDeletedAtBefore(dateCaptor.capture());
            LocalDateTime capturedDate = dateCaptor.getValue();

            // Assert 2: Verify the math.
            // We use a 1-second buffer because the CPU takes a few milliseconds
            // to run between the scheduler executing and this assertion running.
            LocalDateTime expectedDate = LocalDateTime.now().minusDays(30);
            long differenceInSeconds = Math.abs(ChronoUnit.SECONDS.between(capturedDate, expectedDate));

            assertTrue(differenceInSeconds <= 1,
                    "Calculated cutoff date should be exactly 30 days ago (within 1 second buffer)");
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
            // Arrange: Pretend the database is empty
            when(repository.deleteByDeletedAtBefore(any(LocalDateTime.class))).thenReturn(0);

            // Act
            scheduler.cleanupDeletedUrls();

            // Assert: Verify it still successfully calls the repository without crashing
            verify(repository, times(1)).deleteByDeletedAtBefore(any(LocalDateTime.class));
        }
    }
}