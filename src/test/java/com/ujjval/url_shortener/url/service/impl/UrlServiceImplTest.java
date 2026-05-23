package com.ujjval.url_shortener.url.service.impl;

import com.ujjval.url_shortener.cache.service.UrlCacheService;
import com.ujjval.url_shortener.exception.*;
import com.ujjval.url_shortener.idgenerator.context.IdGenerationContext;
import com.ujjval.url_shortener.url.dto.UrlRequestDto;
import com.ujjval.url_shortener.url.dto.UrlResponseDto;
import com.ujjval.url_shortener.url.entity.UrlMapping;
import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Nested;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.redisson.api.RBloomFilter;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@DisplayName("URL Module - Service Layer Test Suite")
public class UrlServiceImplTest {
    @Mock
    private UrlMappingRepository repository;
    private AutoCloseable closeable;
    @Mock
    private IdGenerationContext idGenerationContext;
    @Mock
    private UrlCacheService urlCacheService;
    @Mock
    private RBloomFilter<String> shortCodeBloomFilter;

    @InjectMocks
    private UrlServiceImpl urlService;

    @BeforeEach
    void setUp() {
        //closeable = MockitoAnnotations.openMocks(this);
        MockitoAnnotations.openMocks(this);
        urlService = new UrlServiceImpl(repository, idGenerationContext, urlCacheService, shortCodeBloomFilter);

        ReflectionTestUtils.setField(urlService, "baseUrl", "http://localhost:8080");
        ReflectionTestUtils.setField(urlService, "defaultRetentionYears", 1L);
    }
//    @AfterEach
//    void tearDown() throws Exception {
//        closeable.close();
//    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 1:
            Should create short URL successfully
    
            Expected Result:
            URL mapping saved, short URL generated, and added to Bloom Filter
            """
    )
    void shouldCreateShortUrlSuccessfully() {
        UrlRequestDto request = new UrlRequestDto();
        request.setOriginalUrl("https://google.com");
        when(idGenerationContext.generateId()).thenReturn(123456789L);

        UrlResponseDto response = urlService.shortenUrl(request);

        assertNotNull(response);
        assertEquals("https://google.com", response.getOriginalUrl());
        assertNotNull(response.getShortCode());

        verify(repository, times(1)).save(any(UrlMapping.class));
        verify(shortCodeBloomFilter, times(1)).add(anyString()); // Verify Bloom Filter was updated
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 2:
            Should use custom alias when provided (Bloom Filter Fast-Pass)
    
            Expected Result:
            Custom alias should be used, DB check should be skipped,
            and URL mapping should be saved successfully
            """
    )
    void shouldUseCustomAliasWhenProvided(){
        UrlRequestDto request = new UrlRequestDto();
        request.setOriginalUrl("https://google.com");
        request.setCustomAlias("mygoogle");

        when(idGenerationContext.generateId()).thenReturn(123456789L);
        // Bloom Filter says alias is available!
        when(shortCodeBloomFilter.contains("mygoogle")).thenReturn(false);

        UrlResponseDto response = urlService.shortenUrl(request);
        assertTrue(response.getShortUrl().contains("mygoogle"));

        verify(repository, times(1)).save(any(UrlMapping.class));
        verify(repository, never()).existsByShortCode("mygoogle"); // Verifies our DB Optimization worked!
        verify(shortCodeBloomFilter, times(1)).add("mygoogle");
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 3:
            Should throw exception when custom alias already exists
    
            Expected Result:
            AliasAlreadyExistsException should be thrown
            after Bloom Filter and DB confirm collision
            """
    )
    void shouldThrowExceptionWhenAliasAlreadyExists(){
        UrlRequestDto request = new UrlRequestDto();
        request.setOriginalUrl("https://google.com");
        request.setCustomAlias("mygoogle");

        // Bloom Filter says it might exist
        when(shortCodeBloomFilter.contains("mygoogle")).thenReturn(true);
        // DB confirms it DOES exist
        when(repository.existsByShortCode("mygoogle")).thenReturn(true);

        AliasAlreadyExistsException exception = assertThrows(
                AliasAlreadyExistsException.class,
                ()->urlService.shortenUrl(request)
        );

        assertEquals(UrlErrorCode.ALIAS_EXISTS, exception.getErrorCode());
        verify(repository, never()).save(any(UrlMapping.class));
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 4:
            Should return original URL successfully from database
    
            Expected Result:
            Original URL should be fetched from database,
            click count should be incremented,
            and URL should be cached
            """
    )
    void shouldReturnOriginalUrlSuccessfully(){
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode("abc123");
        urlMapping.setOriginalUrl("https://google.com");
        urlMapping.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(shortCodeBloomFilter.contains("abc123")).thenReturn(true); // Let it pass the shield
        when(urlCacheService.get("abc123")).thenReturn(null);
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(urlMapping));

        String originalUrl = urlService.getOriginalUrl("abc123");
        assertEquals("https://google.com", originalUrl);
        verify(repository, times(1)).incrementClickCount("abc123");
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 5:
            Should return original URL successfully from cache
    
            Expected Result:
            Original URL should be fetched from cache
            without querying database
            """
    )
    void shouldReturnOriginalUrlFromCache(){
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode("abc123");
        urlMapping.setOriginalUrl("https://google.com");
        urlMapping.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(shortCodeBloomFilter.contains("abc123")).thenReturn(true); // Let it pass the shield
        when(urlCacheService.get("abc123")).thenReturn("https://google.com");
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(urlMapping));

        String originalUrl = urlService.getOriginalUrl("abc123");
        assertEquals("https://google.com", originalUrl);
        verify(repository, never()).findByShortCode(any());
        verify(repository, times(1)).incrementClickCount("abc123");
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 6:
            Should throw URL not found exception (Bloom Filter False Positive)
    
            Expected Result:
            Should throw LightweightUrlNotFoundException (Bloom Filter False Positive)
            when short code passes Bloom Filter but does not exist in DB
            """
    )
    void shouldThrowUrlNotFoundException(){
        when(shortCodeBloomFilter.contains("invalid123")).thenReturn(true); // Simulating False Positive
        when(urlCacheService.get("invalid123")).thenReturn(null);
        when(repository.findByShortCode("invalid123")).thenReturn(Optional.empty());

        LightweightUrlNotFoundException exception = assertThrows(
                LightweightUrlNotFoundException.class,
                ()->urlService.getOriginalUrl("invalid123")
        );

        assertEquals(UrlErrorCode.URL_NOT_FOUND, exception.getErrorCode());
        verify(repository, times(1)).findByShortCode("invalid123");
        verify(repository, never()).incrementClickCount(any());
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 7:
            Should throw expired URL exception
    
            Expected Result:
            ExpiredUrlException should be thrown
            when URL expiration time has passed
            """
    )
    void shouldThrowExpiredUrlException(){
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode("expired123");
        urlMapping.setOriginalUrl("https://google.com");
        urlMapping.setExpiresAt(LocalDateTime.now().minusDays(1));

        when(shortCodeBloomFilter.contains("expired123")).thenReturn(true); // Let it pass the shield
        when(urlCacheService.get("expired123")).thenReturn(null);
        when(repository.findByShortCode("expired123")).thenReturn((Optional.of(urlMapping)));

        ExpiredUrlException exception = assertThrows(
                ExpiredUrlException.class,
                () ->urlService.getOriginalUrl("expired123")
        );

        assertEquals(UrlErrorCode.URL_EXPIRED, exception.getErrorCode());
        verify(repository, times(1)).findByShortCode("expired123");
        verify(repository, never()).incrementClickCount(any());
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 8:
            Should soft delete URL successfully
    
            Expected Result:
            URL should be marked as deleted
            and cache entry should be removed
            """
    )
    void shouldSoftDeleteUrlSuccessfully(){
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode("abc123");
        urlMapping.setDeletedAt(null);

        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(urlMapping));

        urlService.softDelete("abc123");
        assertNotNull(urlMapping.getDeletedAt());
        verify(repository, times(1)).save(urlMapping);
        verify(urlCacheService, times(1)).delete("abc123");
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 9:
            Should throw exception when URL is already deleted
    
            Expected Result:
            UrlNotFoundException should be thrown
            when attempting to delete an already deleted URL
            """
    )
    void shouldThrowExceptionWhenUrlAlreadyDeleted(){
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode("abc123");
        urlMapping.setDeletedAt(LocalDateTime.now());
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(urlMapping));

        UrlNotFoundException exception = assertThrows(
                UrlNotFoundException.class,
                ()->urlService.softDelete("abc123")
        );
        assertEquals(UrlErrorCode.URL_DELETED, exception.getErrorCode());
        verify(repository, never()).save(any());
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 10:
            Should restore deleted URL successfully
    
            Expected Result:
            Deleted URL should be restored successfully
            and cache should be updated
            """
    )
    void shouldRestoreUrlSuccessfully(){
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode("abc123");
        urlMapping.setOriginalUrl("https://google.com");
        urlMapping.setDeletedAt(LocalDateTime.now());
        urlMapping.setExpiresAt(LocalDateTime.now().plusDays(1));

        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(urlMapping));
        urlService.restoreUrl("abc123");

        assertNull(urlMapping.getDeletedAt());
        verify(repository , times(1)).save(urlMapping);
        verify(urlCacheService, times(1)).save(
                eq("abc123"),
                eq("https://google.com"),
                any(LocalDateTime.class)
        );
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 11:
            Should throw exception when restoring non deleted URL
    
            Expected Result:
            InvalidUrlException should be thrown
            when attempting to restore an active URL
            """
    )
    void shouldThrowExceptionWhenRestoringNonDeletedUrl() {
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setShortCode("abc123");
        urlMapping.setDeletedAt(null);
        when(repository.findByShortCode("abc123")).thenReturn(Optional.of(urlMapping));

        InvalidUrlException exception = assertThrows(
                InvalidUrlException.class,
                () -> urlService.restoreUrl("abc123")
        );
        assertEquals(UrlErrorCode.INVALID_URL, exception.getErrorCode());
        verify(repository, never()).save(any());
    }

    @Test
    @Nested
    @DisplayName(
            """
            Test Case 12:
            Should reject URL immediately using Bloom Filter Shield
    
            Expected Result:
            UrlNotFoundException should be thrown and short-circuited 
            before querying Cache or Database
            """
    )
    void shouldRejectImmediatelyUsingBloomFilter() {
        String fakeCode = "hacker123";
        // Bloom Filter says NO (Definitive rejection)
        when(shortCodeBloomFilter.contains(fakeCode)).thenReturn(false);

        LightweightUrlNotFoundException exception = assertThrows(
                LightweightUrlNotFoundException.class,
                () -> urlService.getOriginalUrl(fakeCode)
        );

        assertEquals(UrlErrorCode.URL_NOT_FOUND, exception.getErrorCode());

        // Verify the shield completely bypassed the downstream systems
//        verify(urlCacheService, never()).get(anyString());
//        verify(repository, never()).findByShortCode(anyString());
        verifyNoInteractions(urlCacheService);
        verifyNoInteractions(repository);
    }
}