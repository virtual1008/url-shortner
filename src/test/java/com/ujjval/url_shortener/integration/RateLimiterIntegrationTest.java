package com.ujjval.url_shortener.integration;

import com.ujjval.url_shortener.ratelimit.RateLimitFilter;
import com.ujjval.url_shortener.ratelimit.strategy.RateLimiterStrategy;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.util.Map;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "application.rate-limit.capacity=5"
})
public class RateLimiterIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private RateLimiterStrategy rateLimiterStrategy;

    @BeforeEach
    void resetRateLimiter() {
        rateLimiterStrategy.reset(); // Beautifully simple and readable
        this.mockMvc = MockMvcBuilders.webAppContextSetup(context)
                .addFilter(new RateLimitFilter(rateLimiterStrategy), "/*")
                .build();
    }

    @AfterEach
    void tearDown() {
        // Access the private 'limiters' map inside InMemoryRateLimiterStrategy
        // and clear it without changing production code.
        Map<?, ?> limiters = (Map<?, ?>) ReflectionTestUtils.getField(rateLimiterStrategy, "limiters");
        if (limiters != null) {
            limiters.clear();
        }
    }

    @Test
    @DisplayName(
            """
            Test Case 1:
            Should throttle requests when in-memory capacity is exceeded

            Expected Result:
            HTTP 429 Too Many Requests response should be returned
            after the rate limit capacity of 5 is reached
            """
    )
    void shouldThrottleRequestsWhenCapacityExceeded() throws Exception {
        String requestJson = "{\"originalUrl\":\"https://google.com\"}";
        String clientIp = "192.168.1.1";
        int capacity = 5; // SET THIS TO MATCH YOUR 'X' value

        // 1. Exhaust the bucket
        for (int i = 0; i < capacity; i++) {
            mockMvc.perform(post("/api/v1/url/shorten")
                            .header("X-Forwarded-For", clientIp)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(requestJson))
                    .andExpect(status().isOk());
        }

        // 2. The next request MUST be throttled
        mockMvc.perform(post("/api/v1/url/shorten")
                        .header("X-Forwarded-For", clientIp)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isTooManyRequests());
    }
}