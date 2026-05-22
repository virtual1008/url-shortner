package com.ujjval.url_shortener;

import com.ujjval.url_shortener.integration.BloomFilterIntegrationTest;
import com.ujjval.url_shortener.integration.RedisCacheIntegrationTest;
import com.ujjval.url_shortener.url.controller.UrlShortenerControllerTest;
import com.ujjval.url_shortener.url.repository.UrlMappingRepositoryTest;
import com.ujjval.url_shortener.url.service.impl.UrlServiceImplTest;
import com.ujjval.url_shortener.integration.FullUrlFlowIntegrationTest;
import org.junit.platform.suite.api.SelectClasses;
import org.junit.platform.suite.api.Suite;
import org.junit.platform.suite.api.SuiteDisplayName;

@Suite
@SuiteDisplayName("LinkScale Master Test Suite - Ordered Execution")
@SelectClasses({
        // 1. REPOSITORY LAYER (Fastest: Checks if SQL/DB mapping is broken)
        UrlMappingRepositoryTest.class,

        // 2. SERVICE LAYER (Core Business Logic)
        UrlServiceImplTest.class,

        // 3. CONTROLLER LAYER (API Routing & Validation)
        UrlShortenerControllerTest.class,

        // 4. INFRASTRUCTURE & INTEGRATION LAYER (Slowest: Full context, Redis, Postgres)
        BloomFilterIntegrationTest.class,
        RedisCacheIntegrationTest.class,
        FullUrlFlowIntegrationTest.class
})
public class MasterTestSuite {
}
