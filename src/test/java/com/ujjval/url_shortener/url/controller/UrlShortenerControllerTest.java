package com.ujjval.url_shortener.url.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ujjval.url_shortener.ratelimit.RateLimitFilter;
import com.ujjval.url_shortener.ratelimit.strategy.RateLimiterStrategy;
import com.ujjval.url_shortener.url.dto.UrlRequestDto;
import com.ujjval.url_shortener.url.dto.UrlResponseDto;
import com.ujjval.url_shortener.url.service.UrlService;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@TestPropertySource(properties = {
        "application.pagination.max-size=100"
})
@DisplayName("URL Module - Controller Layer Test Suite")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
@WebMvcTest(controllers = UrlShortenerController.class,
        excludeFilters = @ComponentScan.Filter(type = FilterType.ASSIGNABLE_TYPE, classes = RateLimitFilter.class))
public class UrlShortenerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private RateLimiterStrategy rateLimiterStrategy;

    @MockBean
    private UrlService urlService;

    @BeforeEach
    void setup() {
        // This now works because we added reset() to the interface!
        rateLimiterStrategy.reset();
    }

    @Autowired
    private ObjectMapper objectMapper;

    @Nested
    @DisplayName("Short URL Creation Tests")
    class ShortenUrlTests {
        @Test
        @DisplayName(
                """
                Test Case 1:
                Should create short URL successfully

                Expected Result:
                HTTP 200 response should be returned
                with generated short URL details
                """
        )
        void shouldCreateShortUrlSuccessfully() throws Exception {
            UrlRequestDto requestDto = new UrlRequestDto();
            requestDto.setOriginalUrl("https://google.com");

            UrlResponseDto responseDto =
                    UrlResponseDto.builder()
                            .originalUrl("https://google.com")
                            .shortCode("abc123")
                            .shortUrl("http://localhost:8080/abc123")
                            .build();

            when(urlService.shortenUrl(any()))
                    .thenReturn(responseDto);

            mockMvc.perform(post("/api/v1/url/shorten")
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(
                                    objectMapper.writeValueAsString(requestDto)
                            )
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.originalUrl")
                            .value("https://google.com"))
                    .andExpect(jsonPath("$.shortCode")
                            .value("abc123"))
                    .andExpect(jsonPath("$.shortUrl")
                            .value("http://localhost:8080/abc123"));

            //verify(urlService).shortenUrl(any());
            verify(urlService, times(1)).shortenUrl(any());
        }
    }

    @Nested
    @DisplayName("URL Redirection Tests")
    class RedirectTests {
        @Test
        @DisplayName(
                """
                Test Case 2:
                Should redirect to original URL successfully

                Expected Result:
                HTTP 301 Permanent Redirect response should be returned
                with original URL location and Cache-Control headers
                """
        )
        void shouldRedirectToOriginalUrlSuccessfully() throws Exception {
            when(urlService.getOriginalUrl("abc123"))
                    .thenReturn("https://google.com");

            mockMvc.perform(get("/api/v1/url/abc123"))
                    .andExpect(status().isMovedPermanently()) // Specifically checking for 301
                    .andExpect(header().string(HttpHeaders.LOCATION, "https://google.com"))
                    .andExpect(header().string(HttpHeaders.CACHE_CONTROL, "max-age=3600, public")); // Verifying our new caching strategy

            //verify(urlService).getOriginalUrl("abc123");
            verify(urlService, times(1)).getOriginalUrl("abc123");
        }
    }

    @Nested
    @DisplayName("Soft Delete Tests")
    class SoftDeleteTests {
        @Test
        @DisplayName(
                """
                Test Case 3:
                Should soft delete URL successfully

                Expected Result:
                HTTP 200 response should be returned
                after successful soft deletion
                """
        )
        void shouldSoftDeleteUrlSuccessfully() throws Exception {
            doNothing().when(urlService).softDelete("abc123");

            mockMvc.perform(delete("/api/v1/url/abc123"))
                    .andExpect(status().isOk());

            //verify(urlService).softDelete("abc123");
            verify(urlService, times(1)).softDelete("abc123");
        }
    }

    @Nested
    @DisplayName("URL Restore Tests")
    class RestoreTests {
        @Test
        @DisplayName(
                """
                Test Case 4:
                Should restore URL successfully

                Expected Result:
                HTTP 200 response should be returned
                after successful restoration
                """
        )
        void shouldRestoreUrlSuccessfully() throws Exception {
            doNothing().when(urlService)
                    .restoreUrl("abc123");
            mockMvc.perform(
                            put("/api/v1/url/restore/abc123")
                    )
                    .andExpect(status().isOk());
            verify(urlService).restoreUrl("abc123");
        }
    }

    @Nested
    @DisplayName("Pagination Tests")
    class PaginationTests {
        @Test
        @DisplayName(
                """
                Test Case 5:
                Should return paginated list of URLs

                Expected Result:
                HTTP 200 response should be returned
                with paginated URL data
                """
        )
        void shouldReturnPaginatedUrlsSuccessfully() throws Exception {
            UrlResponseDto responseDto =
                    UrlResponseDto.builder()
                            .originalUrl("https://google.com")
                            .shortCode("abc123")
                            .shortUrl("http://localhost:8080/abc123")
                            .build();
            Page<UrlResponseDto> page = new PageImpl<>(
                    List.of(responseDto),
                    PageRequest.of(0, 20),
                    1
            );
            when(urlService.getAllUrls(any()))
                    .thenReturn(page);
            mockMvc.perform(get("/api/v1/url")
                            .param("page", "0")
                            .param("size", "20")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].shortCode")
                            .value("abc123"));

            verify(urlService).getAllUrls(any());
        }

        @Test
        @DisplayName(
                """
                Test Case 6:
                Should return paginated trash URLs successfully

                Expected Result:
                HTTP 200 response should be returned
                with soft deleted URLs
                """
        )
        void shouldReturnTrashUrlsSuccessfully() throws Exception {
            UrlResponseDto responseDto =
                    UrlResponseDto.builder()
                            .originalUrl("https://google.com")
                            .shortCode("trash123")
                            .shortUrl("http://localhost:8080/trash123")
                            .build();

            Page<UrlResponseDto> page = new PageImpl<>(
                    List.of(responseDto),
                    PageRequest.of(0, 20),
                    1
            );

            when(urlService.getUrlsReadyForHardDelete(any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/v1/url/trash")
                            .param("page", "0")
                            .param("size", "20")
                    )
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content[0].shortCode")
                            .value("trash123"));

            verify(urlService).getUrlsReadyForHardDelete(any());
        }
    }
}