package com.ujjval.url_shortener.integration;

import org.junit.jupiter.api.*;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
@SpringBootTest
@TestPropertySource(properties = {
        "spring.flyway.enabled=false",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
@DisplayName("Bloom Filter - Integration Test Suite")
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
public class BloomFilterIntegrationTest {
    @Autowired
    private RBloomFilter<String> shortCodeBloomFilter;

    @AfterEach
    void tearDown(){
        shortCodeBloomFilter.delete();
        shortCodeBloomFilter.tryInit(10_000_000L, 0.01);
    }

    @Nested
    @DisplayName("Initialization Tests")
    class InitializationTests {
       @Test
       @DisplayName(
               """
                Test Case 1:
                Should successfully initialize Bloom Filter bean

                Expected Result:
                The RBloomFilter is injected, not null, and properly configured                       
               """
       )
        void shouldInitializeBloomFilterSuccessfully(){
             assertNotNull(shortCodeBloomFilter,"Bloom Filter bean should be successfully injected");
             assertTrue(shortCodeBloomFilter.isExists(),"Bloom Filter should exist in Redis");
             assertEquals(10_000_000L, shortCodeBloomFilter.getExpectedInsertions());
             assertEquals(0.01, shortCodeBloomFilter.getFalseProbability());
        }

    }

    @Nested
    @DisplayName("Core Functionality Tests")
    class CoreFunctionalityTests{
        @Test
        @DisplayName(
                """
                Test Case 2:
                Should accurately reflect added elements

                Expected Result:
                contains() returns false before adding, and true after adding
                """
        )
        void shouldAccuratelyReflectAddedElements() {
            String testCode = "newAlias123";

            assertFalse(shortCodeBloomFilter.contains(testCode),"Bloom filter should return false for an alias that hasn't been added");
            assertTrue(shortCodeBloomFilter.add(testCode),"Adding a new element should return true");
            assertTrue(shortCodeBloomFilter.contains(testCode),"Bloom filter should return true after the alias is added");
        }


        @Test
        @DisplayName(
                """
                Test Case 3:
                Should reject adding duplicate elements

                Expected Result:
                add() returns false when adding an element that is already in the filter
                """
        )
        void shouldRejectAddingDuplicateElements() {
            String duplicateCode = "duplicateAlias";

            shortCodeBloomFilter.add(duplicateCode);

            boolean isAddedAgain = shortCodeBloomFilter.add(duplicateCode);

            assertFalse(isAddedAgain,"Adding an already existing element should return false");
        }
    }
}
