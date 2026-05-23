package com.ujjval.url_shortener;

import com.ujjval.url_shortener.integration.BloomFilterIntegrationTest;
import com.ujjval.url_shortener.integration.RedisCacheIntegrationTest;
import com.ujjval.url_shortener.integration.RateLimiterIntegrationTest; // <--- ADDED
import com.ujjval.url_shortener.integration.FullUrlFlowIntegrationTest;
import com.ujjval.url_shortener.url.controller.UrlShortenerControllerTest;
import com.ujjval.url_shortener.url.repository.UrlMappingRepositoryTest;
import com.ujjval.url_shortener.url.service.impl.UrlServiceImplTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.springframework.context.ApplicationContext;

@Suite
@SuiteDisplayName("LinkScale Master Test Suite - Full Regression")
@SelectClasses({
        // 1. REPOSITORY LAYER (Foundational data access)
        UrlMappingRepositoryTest.class,

        // 2. SERVICE LAYER (Business logic & Shield Pattern)
        UrlServiceImplTest.class,

        // 3. CONTROLLER LAYER (API contract validation)
        UrlShortenerControllerTest.class,

        // 4. INFRASTRUCTURE & MIDDLEWARE (Rate Limiting & Caching)
        BloomFilterIntegrationTest.class,
        RedisCacheIntegrationTest.class,
        RateLimiterIntegrationTest.class, // <--- ADDED

        // 5. END-TO-END FLOW (Full lifecycle verification)
        FullUrlFlowIntegrationTest.class
})
public class MasterTestSuite {
    private static ApplicationContext context;

    @BeforeAll
    static void setup() {
        // This is a global hook to clear the Rate Limiter before the suite starts
        // Note: You might need to adjust this depending on how you initialize the suite
    }
}