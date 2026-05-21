package com.ujjval.url_shortener.integration;

import com.ujjval.url_shortener.url.entity.UrlMapping;
import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.data.redis.core.StringRedisTemplate;

import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
@Transactional
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.data.redis.host=localhost",
        "spring.data.redis.port=6379"
})
@DisplayName("URL Module - Redis Cache Integration Test Suite")
@DisplayNameGeneration(
        DisplayNameGenerator.ReplaceUnderscores.class
)
public class RedisCacheIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UrlMappingRepository repository;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @PersistenceContext
    private EntityManager entityManager;

    @Nested
    @DisplayName("Redis Cache Flow Tests")
    class RedisCacheFlowTests {

        @Test
        @DisplayName(
                """
                Test Case 1:
                Should populate Redis cache after cache miss

                Expected Result:
                - First request should fetch URL from database
                - URL should be stored inside Redis cache
                - Click count should increment successfully
                """
        )
        void shouldPopulateRedisCacheAfterCacheMiss()
                throws Exception {


            UrlMapping urlMapping = new UrlMapping();

            urlMapping.setId(1001L);
            urlMapping.setShortCode("cache123");
            urlMapping.setOriginalUrl("https://google.com");

            urlMapping.setExpiresAt(
                    LocalDateTime.now().plusDays(1)
            );

            repository.save(urlMapping);
            redisTemplate.delete("cache123");

            mockMvc.perform(get("/api/v1/url/cache123")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(
                            header().string(
                                    "Location",
                                    "https://google.com"
                            )
                    );

            entityManager.flush();
            entityManager.clear();


            String cachedValue =
                    redisTemplate.opsForValue()
                            .get("url:cache123");

            assertNotNull(
                    cachedValue,
                    "URL should be stored in Redis cache"
            );

            assertEquals(
                    "https://google.com",
                    cachedValue,
                    "Cached URL should match original URL"
            );

            UrlMapping updatedUrl =
                    repository.findByShortCode("cache123")
                            .orElseThrow();

            assertEquals(
                    1L,
                    updatedUrl.getClickCount(),
                    "Click count should increment after redirect"
            );
        }

        @Test
        @DisplayName(
                """
                Test Case 2:
                Should retrieve URL successfully from Redis cache

                Expected Result:
                - First request should populate cache
                - Second request should use Redis cache
                - Click count should increment correctly
                """
        )
        void shouldRetrieveUrlSuccessfullyFromRedisCache()
                throws Exception {

            UrlMapping urlMapping = new UrlMapping();

            urlMapping.setId(2001L);
            urlMapping.setShortCode("redis123");
            urlMapping.setOriginalUrl("https://google.com");

            urlMapping.setExpiresAt(
                    LocalDateTime.now().plusDays(1)
            );

            repository.save(urlMapping);
            redisTemplate.delete("redis123");

            // =========================================================
            // FIRST REQUEST -> CACHE MISS
            // =========================================================

            mockMvc.perform(
                            get("/api/v1/url/redis123")
                    )
                    .andExpect(status().is3xxRedirection());

            // =========================================================
            // VERIFY CACHE POPULATED
            // =========================================================

            String cachedValue =
                    redisTemplate.opsForValue()
                            .get("url:redis123");

            assertNotNull(
                    cachedValue,
                    "Redis cache should contain URL after first request"
            );

            // =========================================================
            // SECOND REQUEST -> CACHE HIT
            // =========================================================

            mockMvc.perform(
                            get("/api/v1/url/redis123")
                    )
                    .andExpect(status().is3xxRedirection())
                    .andExpect(
                            header().string(
                                    "Location",
                                    "https://google.com"
                            )
                    );

            entityManager.flush();
            entityManager.clear();

            UrlMapping updatedUrl =
                    repository.findByShortCode("redis123")
                            .orElseThrow();

            assertEquals(
                    2L,
                    updatedUrl.getClickCount(),
                    "Click count should increment on both requests"
            );
        }

        @Test
        @DisplayName(
                """
                Test Case 3:
                Should evict Redis cache after soft delete

                Expected Result:
                - URL should initially exist in Redis cache
                - Soft delete should remove Redis cache entry
                - Cache key should no longer exist
                """
        )
        void shouldEvictRedisCacheAfterSoftDelete()
                throws Exception {

            UrlMapping urlMapping = new UrlMapping();

            urlMapping.setId(3001L);
            urlMapping.setShortCode("delete123");
            urlMapping.setOriginalUrl("https://google.com");

            urlMapping.setExpiresAt(
                    LocalDateTime.now().plusDays(1)
            );

            repository.save(urlMapping);

            // =========================================================
            // POPULATE CACHE
            // =========================================================

            mockMvc.perform(
                            get("/api/v1/url/delete123")
                    )
                    .andExpect(status().is3xxRedirection());

            String cachedBeforeDelete =
                    redisTemplate.opsForValue()
                            .get("url:delete123");

            assertNotNull(
                    cachedBeforeDelete,
                    "Redis cache should contain URL before deletion"
            );

            // =========================================================
            // DELETE URL
            // =========================================================

            mockMvc.perform(
                            delete("/api/v1/url/delete123")
                    )
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            // =========================================================
            // ASSERT CACHE EVICTED
            // =========================================================

            String cachedAfterDelete =
                    redisTemplate.opsForValue()
                            .get("url:delete123");

            assertNull(
                    cachedAfterDelete,
                    "Redis cache should be evicted after soft delete"
            );
        }

        @Test
        @DisplayName(
                """
                Test Case 4:
                Should store Redis cache with TTL

                Expected Result:
                - Cache entry should exist
                - Redis TTL should be greater than zero
                """
        )
        void shouldStoreRedisCacheWithTTL()
                throws Exception {


            UrlMapping urlMapping = new UrlMapping();

            urlMapping.setId(4001L);
            urlMapping.setShortCode("ttl123");
            urlMapping.setOriginalUrl("https://google.com");

            urlMapping.setExpiresAt(
                    LocalDateTime.now().plusMinutes(10)
            );

            repository.save(urlMapping);

            mockMvc.perform(
                            get("/api/v1/url/ttl123")
                    )
                    .andExpect(status().is3xxRedirection());


            Long ttl =
                    redisTemplate.getExpire(
                            "url:ttl123"
                    );

            assertNotNull(
                    ttl,
                    "Redis TTL should exist"
            );

            assertTrue(
                    ttl > 0,
                    "Redis TTL should be greater than zero"
            );
        }
    }
}