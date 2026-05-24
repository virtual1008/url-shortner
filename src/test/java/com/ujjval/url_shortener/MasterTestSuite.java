package com.ujjval.url_shortener;

import com.ujjval.url_shortener.integration.*;
import com.ujjval.url_shortener.scheduler.UrlCleanupSchedulerTest;
import com.ujjval.url_shortener.url.controller.UrlShortenerControllerTest;
import com.ujjval.url_shortener.url.repository.UrlMappingRepositoryTest;
import com.ujjval.url_shortener.url.service.impl.UrlServiceImplTest;
import org.junit.jupiter.api.BeforeAll;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;
import org.springframework.context.ApplicationContext;

/**
 * LinkScale Master Test Suite
 * * This suite executes the entire automated testing pyramid for the application.
 * Tests are ordered strategically from fastest/most isolated (Unit) to
 * slowest/most complex (Integration & E2E) to ensure rapid failure feedback
 * during the CI/CD pipeline build process.
 */
@Suite
@SuiteDisplayName("LinkScale Master Test Suite - Full Regression")
@SelectClasses({
        // ==========================================
        // TIER 1: UNIT TESTS (Fastest execution)
        // ==========================================

        // 1. REPOSITORY LAYER (Foundational Data Access)
        // Verifies custom JPA queries, soft deletes, and pagination logic
        UrlMappingRepositoryTest.class,

        // 2. SERVICE LAYER (Business Logic & Shield Pattern)
        // Verifies Snowflake ID generation, URL validation, and cache-aside logic
        UrlServiceImplTest.class,

        // 3. CONTROLLER LAYER (API Contract & Routing)
        // Verifies HTTP status codes, JSON serialization, and Spring MVC routing
        UrlShortenerControllerTest.class,

        // 4. SCHEDULER UNIT TESTS
        // Verifies cron job business logic in isolation
        UrlCleanupSchedulerTest.class,

        // ==========================================
        // TIER 2: INTEGRATION TESTS (Middleware & State)
        // ==========================================

        // 5. INFRASTRUCTURE & MIDDLEWARE
        // Verifies interactions with external dependencies (Redis, Database constraints)
        BloomFilterIntegrationTest.class,           // Tests probabilistic existence checks
        RedisCacheIntegrationTest.class,            // Tests distributed caching limits and TTL
        RateLimiterIntegrationTest.class,           // Tests Bucket4j / Redis API throttling
        UrlCleanupSchedulerIntegrationTest.class,   // Tests actual database purging via cron

        // ==========================================
        // TIER 3: ANALYTICS PIPELINE (Asynchronous)
        // ==========================================

        // 6. EVENT STREAMING & TELEMETRY (TODO: Add these once written!)
        // AnalyticsRepositoryTest.class,           // Tests PostgreSQL aggregation queries
        // AnalyticsControllerTest.class,           // Tests Time-Series JSON payloads
        // KafkaAnalyticsIntegrationTest.class,     // Tests asynchronous queue batch flushing

        // ==========================================
        // TIER 4: END-TO-END FLOW (Slowest execution)
        // ==========================================

        // 7. FULL LIFECYCLE VERIFICATION
        // Simulates a real user journey: Shorten -> Cache -> Redirect -> Analytics Queue
        FullUrlFlowIntegrationTest.class
})
public class MasterTestSuite {

    private static ApplicationContext context;

    /**
     * Global Suite Initialization Hook
     * Executes exactly once before any tests in the suite begin.
     * Useful for clearing external state (like flushing a Redis test database)
     * or resetting in-memory rate limiters to ensure a pristine testing environment.
     */
    @BeforeAll
    static void setup() {
        // Example: Clear in-memory Rate Limiter caches
        // Example: Flush test Redis instance
    }
}