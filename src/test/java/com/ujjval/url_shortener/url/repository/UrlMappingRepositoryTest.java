package com.ujjval.url_shortener.url.repository;

import com.ujjval.url_shortener.url.entity.UrlMapping;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
@DisplayName("URL Module - Repository Layer Test Suite")
class UrlMappingRepositoryTest {

    @Autowired
    private UrlMappingRepository repository;

    @PersistenceContext
    private EntityManager entityManager;
    @Nested
    @DisplayName("Find URL By Short Code Tests")
    class FindByShortCodeTests {

        @Test
        @DisplayName(
                """
                Test Case 1:
                Should find URL mapping by short code successfully

                Expected Result:
                URL mapping should be returned
                when valid short code exists
                """
        )
        void shouldFindUrlByShortCodeSuccessfully() {
            UrlMapping urlMapping = new UrlMapping();
            urlMapping.setId(1L);
            urlMapping.setShortCode("abc123");
            urlMapping.setOriginalUrl("https://google.com");
            urlMapping.setExpiresAt(LocalDateTime.now().plusDays(1));
            repository.save(urlMapping);
            Optional<UrlMapping> result = repository.findByShortCode("abc123");
            assertTrue(
                    result.isPresent(),
                    "URL mapping should be present"
            );
            assertEquals(
                    "https://google.com",
                    result.get().getOriginalUrl(),
                    "Original URL should match expected value"
            );
        }
        @Test
        @DisplayName(
                """
                Test Case 2:
                Should return empty result when short code does not exist

                Expected Result:
                Empty Optional should be returned
                """
        )
        void shouldReturnEmptyWhenShortCodeDoesNotExist() {
            Optional<UrlMapping> result = repository.findByShortCode("invalid123");
            assertFalse(
                    result.isPresent(),
                    "Result should be empty"
            );
        }
    }

    @Nested
    @DisplayName("Exists By Short Code Tests")
    class ExistsByShortCodeTests {
        @Test
        @DisplayName(
                """
                Test Case 3:
                Should return true when short code exists

                Expected Result:
                Repository should confirm existence of short code
                """
        )
        void shouldReturnTrueWhenShortCodeExists() {
            UrlMapping urlMapping = new UrlMapping();
            urlMapping.setId(2L);
            urlMapping.setShortCode("exists123");
            urlMapping.setOriginalUrl("https://google.com");
            urlMapping.setExpiresAt(LocalDateTime.now().plusDays(1));
            repository.save(urlMapping);
            boolean exists = repository.existsByShortCode("exists123");
            assertTrue(
                    exists,
                    "Short code should exist"
            );
        }

        @Test
        @DisplayName(
                """
                Test Case 4:
                Should return false when short code does not exist

                Expected Result:
                Repository should confirm short code does not exist
                """
        )
        void shouldReturnFalseWhenShortCodeDoesNotExist() {
            boolean exists = repository.existsByShortCode("notfound123");
            assertFalse(
                    exists,
                    "Short code should not exist"
            );
        }
    }

    @Nested
    @DisplayName("Increment Click Count Tests")
    class IncrementClickCountTests {
        @Test
        @DisplayName(
                """
                Test Case 5:
                Should increment click count successfully

                Expected Result:
                Click count should increase by one
                """
        )
        void shouldIncrementClickCountSuccessfully() {
            UrlMapping urlMapping = new UrlMapping();
            urlMapping.setId(3L);
            urlMapping.setShortCode("click123");
            urlMapping.setOriginalUrl("https://google.com");
            urlMapping.setClickCount(0L);
            urlMapping.setExpiresAt(
                    LocalDateTime.now().plusDays(1)
            );
            repository.save(urlMapping);
            repository.incrementClickCount("click123");
            entityManager.flush();
            entityManager.clear();
            UrlMapping updated = repository.findByShortCode("click123")
                            .orElseThrow();
            assertEquals(
                    1L,
                    updated.getClickCount(),
                    "Click count should be incremented"
            );
        }
    }

    @Nested
    @DisplayName("Soft Deleted URL Tests")
    class SoftDeletedUrlTests {
        @Test
        @DisplayName(
                """
                Test Case 6:
                Should find only non deleted URL by short code

                Expected Result:
                Active URL should be returned successfully
                """
        )
        void shouldFindOnlyNonDeletedUrl() {
            UrlMapping urlMapping = new UrlMapping();
            urlMapping.setId(4L);
            urlMapping.setShortCode("active123");
            urlMapping.setOriginalUrl("https://google.com");
            urlMapping.setDeletedAt(null);
            urlMapping.setExpiresAt(
                    LocalDateTime.now().plusDays(1)
            );
            repository.save(urlMapping);
            Optional<UrlMapping> result = repository.findByShortCodeAndDeletedAtIsNull(
                            "active123"
                    );
            assertTrue(
                    result.isPresent(),
                    "Active URL should be found"
            );
        }

        @Test
        @DisplayName(
                """
                Test Case 7:
                Should not return soft deleted URL

                Expected Result:
                Empty Optional should be returned
                for soft deleted URL
                """
        )
        void shouldNotReturnSoftDeletedUrl() {
            UrlMapping urlMapping = new UrlMapping();
            urlMapping.setId(5L);
            urlMapping.setShortCode("deleted123");
            urlMapping.setOriginalUrl("https://google.com");
            urlMapping.setDeletedAt(LocalDateTime.now());
            urlMapping.setExpiresAt(
                    LocalDateTime.now().plusDays(1)
            );
            repository.save(urlMapping);
            Optional<UrlMapping> result = repository.findByShortCodeAndDeletedAtIsNull(
                            "deleted123"
                    );
            assertFalse(
                    result.isPresent(),
                    "Soft deleted URL should not be returned"
            );
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {
        @Test
        @DisplayName(
                """
                Test Case 8:
                Should return paginated deleted URLs

                Expected Result:
                Paginated list of soft deleted URLs should be returned
                """
        )
        void shouldReturnPaginatedDeletedUrls() {
            UrlMapping url1 = new UrlMapping();
            url1.setId(6L);
            url1.setShortCode("deleted1");
            url1.setOriginalUrl("https://google.com");
            url1.setDeletedAt(LocalDateTime.now());

            UrlMapping url2 = new UrlMapping();
            url2.setId(7L);
            url2.setShortCode("deleted2");
            url2.setOriginalUrl("https://facebook.com");
            url2.setDeletedAt(LocalDateTime.now());

            repository.save(url1);
            repository.save(url2);

            Page<UrlMapping> result = repository.findByDeletedAtNotNull(
                            PageRequest.of(0, 10)
                    );
            assertEquals(
                    2,
                    result.getTotalElements(),
                    "Total deleted URLs should match expected count"
            );
        }
    }

    @Nested
    @DisplayName("Hard Delete Tests")
    class HardDeleteTests {
        @Test
        @DisplayName(
                """
                Test Case 9:
                Should delete URLs older than retention period

                Expected Result:
                Old soft deleted URLs should be removed permanently
                """
        )
        void shouldDeleteUrlsOlderThanRetentionPeriod() {
            UrlMapping urlMapping = new UrlMapping();
            urlMapping.setId(8L);
            urlMapping.setShortCode("olddeleted");
            urlMapping.setOriginalUrl("https://google.com");
            urlMapping.setDeletedAt(
                    LocalDateTime.now().minusDays(40)
            );
            repository.save(urlMapping);

            int deletedCount = repository.deleteByDeletedAtBefore(
                            LocalDateTime.now().minusDays(30)
                    );
            assertEquals(
                    1,
                    deletedCount,
                    "One record should be deleted"
            );

            Optional<UrlMapping> result =
                    repository.findByShortCode("olddeleted");

            assertFalse(
                    result.isPresent(),
                    "Deleted record should not exist anymore"
            );
        }
    }
}