package com.ujjval.url_shortener.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ujjval.url_shortener.url.dto.UrlRequestDto;
import com.ujjval.url_shortener.url.entity.UrlMapping;
import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.DisplayNameGeneration;
import org.junit.jupiter.api.DisplayNameGenerator;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;

import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureTestDatabase(
        replace = AutoConfigureTestDatabase.Replace.ANY
)
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("URL Module - Full Integration Test Suite")
@DisplayNameGeneration(
        DisplayNameGenerator.ReplaceUnderscores.class
)
@Transactional
class FullUrlFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UrlMappingRepository repository;

    @PersistenceContext
    private EntityManager entityManager;

    @Nested
    @DisplayName("Complete URL Lifecycle Tests")
    class CompleteUrlLifecycleTests {

        @Test
        @DisplayName(
                """
                Test Case 1:
                Should complete full URL lifecycle successfully

                Expected Result:
                URL should be:
                - Created successfully
                - Retrieved successfully
                - Click count incremented
                - Soft deleted successfully
                - Restored successfully
                """
        )
        void shouldCompleteFullUrlLifecycleSuccessfully()
                throws Exception {

            // =========================================================
            // STEP 1 : CREATE SHORT URL
            // =========================================================

            UrlRequestDto requestDto = new UrlRequestDto();
            requestDto.setOriginalUrl("https://google.com");

            String response =
                    mockMvc.perform(
                                    post("/api/v1/url/shorten")
                                            .contentType(
                                                    MediaType.APPLICATION_JSON
                                            )
                                            .content(
                                                    objectMapper.writeValueAsString(
                                                            requestDto
                                                    )
                                            )
                            )
                            .andExpect(status().isOk())
                            .andReturn()
                            .getResponse()
                            .getContentAsString();

            JsonNode jsonNode =
                    objectMapper.readTree(response);

            String shortCode =
                    jsonNode.get("shortCode").asText();

            assertNotNull(
                    shortCode,
                    "Short code should be generated"
            );

            // =========================================================
            // STEP 2 : VERIFY URL SAVED IN DATABASE
            // =========================================================

            Optional<UrlMapping> savedUrl =
                    repository.findByShortCode(shortCode);

            assertTrue(
                    savedUrl.isPresent(),
                    "URL mapping should exist in database"
            );

            assertEquals(
                    "https://google.com",
                    savedUrl.get().getOriginalUrl(),
                    "Original URL should match"
            );

            // =========================================================
            // STEP 3 : REDIRECT USING SHORT URL
            // =========================================================

            mockMvc.perform(
                            get("/api/v1/url/" + shortCode)
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

            // =========================================================
            // STEP 4 : VERIFY CLICK COUNT INCREMENTED
            // =========================================================

            UrlMapping updatedUrl =
                    repository.findByShortCode(shortCode)
                            .orElseThrow();

            assertEquals(
                    1L,
                    updatedUrl.getClickCount(),
                    "Click count should be incremented"
            );

            // =========================================================
            // STEP 5 : SOFT DELETE URL
            // =========================================================

            mockMvc.perform(
                            delete("/api/v1/url/" + shortCode)
                    )
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            UrlMapping deletedUrl =
                    repository.findByShortCode(shortCode)
                            .orElseThrow();

            assertNotNull(
                    deletedUrl.getDeletedAt(),
                    "DeletedAt should not be null after soft delete"
            );

            // =========================================================
            // STEP 6 : RESTORE URL
            // =========================================================

            mockMvc.perform(
                            put("/api/v1/url/restore/" + shortCode)
                    )
                    .andExpect(status().isOk());

            entityManager.flush();
            entityManager.clear();

            UrlMapping restoredUrl =
                    repository.findByShortCode(shortCode)
                            .orElseThrow();

            assertNull(
                    restoredUrl.getDeletedAt(),
                    "DeletedAt should be null after restore"
            );
        }
    }

    @Nested
    @DisplayName("Expiration Flow Tests")
    class ExpirationFlowTests {

        @Test
        @DisplayName(
                """
                Test Case 2:
                Should prevent access to expired URL

                Expected Result:
                Expired URL should not redirect successfully
                """
        )
        void shouldPreventAccessToExpiredUrl()
                throws Exception {

            UrlMapping urlMapping = new UrlMapping();

            urlMapping.setId(999L);
            urlMapping.setShortCode("expired123");
            urlMapping.setOriginalUrl("https://google.com");
            urlMapping.setClickCount(0L);

            urlMapping.setExpiresAt(
                    LocalDateTime.now().minusDays(1)
            );

            repository.save(urlMapping);


            mockMvc.perform(
                            get("/api/v1/url/expired123")
                    )
                    .andExpect(status().is4xxClientError());
        }
    }

    @Nested
    @DisplayName("Soft Delete Protection Tests")
    class SoftDeleteProtectionTests {

        @Test
        @DisplayName(
                """
                Test Case 3:
                Should prevent access to soft deleted URL

                Expected Result:
                Soft deleted URL should not redirect successfully
                """
        )
        void shouldPreventAccessToSoftDeletedUrl()
                throws Exception {


            UrlMapping urlMapping = new UrlMapping();

            urlMapping.setId(1000L);
            urlMapping.setShortCode("deleted123");
            urlMapping.setOriginalUrl("https://google.com");

            urlMapping.setDeletedAt(
                    LocalDateTime.now()
            );

            urlMapping.setExpiresAt(
                    LocalDateTime.now().plusDays(1)
            );

            repository.save(urlMapping);


            mockMvc.perform(
                            get("/api/v1/url/deleted123")
                    )
                    .andExpect(status().is4xxClientError());
        }
    }

}