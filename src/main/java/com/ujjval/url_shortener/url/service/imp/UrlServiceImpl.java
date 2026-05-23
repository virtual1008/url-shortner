package com.ujjval.url_shortener.url.service.impl;

import com.ujjval.url_shortener.cache.service.UrlCacheService;
import com.ujjval.url_shortener.common.util.Base62Encoder;
import com.ujjval.url_shortener.exception.AliasAlreadyExistsException;
import com.ujjval.url_shortener.exception.ExpiredUrlException;
import com.ujjval.url_shortener.exception.InvalidUrlException;
import com.ujjval.url_shortener.exception.UrlNotFoundException;
import com.ujjval.url_shortener.exception.UrlErrorCode; // Added this import
import com.ujjval.url_shortener.idgenerator.context.IdGenerationContext;
import com.ujjval.url_shortener.url.dto.UrlRequestDto;
import com.ujjval.url_shortener.url.dto.UrlResponseDto;
import com.ujjval.url_shortener.url.entity.UrlMapping;
import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import com.ujjval.url_shortener.url.service.UrlService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RBloomFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@Slf4j
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlMappingRepository repository;
    private final IdGenerationContext idGenerationContext;
    private final UrlCacheService urlCacheService;

    private final RBloomFilter<String> shortCodeBloomFilter;
    @Value("${application.base-url}")
    private String baseUrl;

    @Value("${application.retention.default-years}")
    private long defaultRetentionYears;

    @Value("${application.retention.deleted-days:30}")
    private long deletedRetentionDays;

    @Transactional
    @Override
    public UrlResponseDto shortenUrl(UrlRequestDto requestDto) {
        log.debug("Initiating URL shortening process");

        long id = idGenerationContext.generateId();
        UrlMapping urlMapping = new UrlMapping();
        urlMapping.setId(id);
        urlMapping.setOriginalUrl(requestDto.getOriginalUrl());

        String shortCode;

        if (requestDto.getCustomAlias() != null && !requestDto.getCustomAlias().isBlank()) {
            shortCode = requestDto.getCustomAlias();
            log.debug("Evaluating custom alias request: {}", shortCode);
            if (shortCodeBloomFilter.contains(shortCode)) {
                // The filter says "Yes". Because of false positives, we MUST check the DB to be sure.
                log.debug("Bloom filter indicated potential alias collision. Querying database to confirm.");
                if (repository.existsByShortCode(shortCode)) {
                    log.warn("Alias creation rejected: Custom alias '{}' already exists", shortCode);
                    throw new AliasAlreadyExistsException(UrlErrorCode.ALIAS_EXISTS);
                }
            }
        } else {
            shortCode = Base62Encoder.encode(id);
            log.debug("Generated Base62 shortCode: {}", shortCode);
        }
        urlMapping.setShortCode(shortCode);

        LocalDateTime expiresAt;
        if (requestDto.getExpiresInSeconds() != null) {
            expiresAt = LocalDateTime.now().plusSeconds(requestDto.getExpiresInSeconds());
        } else {
            expiresAt = LocalDateTime.now().plusYears(defaultRetentionYears);
        }
        urlMapping.setExpiresAt(expiresAt);

        repository.save(urlMapping);

        shortCodeBloomFilter.add(shortCode);
        log.debug("Added shortCode {} to distributed Bloom Filter", shortCode);

        String shortUrl = baseUrl + "/" + shortCode;

        log.info("Successfully created short URL. ShortCode: {}, ExpiresAt: {}", shortCode, expiresAt);

        return UrlResponseDto.builder()
                .originalUrl(urlMapping.getOriginalUrl())
                .shortCode(shortCode)
                .shortUrl(shortUrl)
                .build();
    }

    @Override
    public String getOriginalUrl(String shortCode) {
        log.debug("Attempting to retrieve original URL for shortCode: {}", shortCode);
        if (!shortCodeBloomFilter.contains(shortCode)) {
            log.warn("Bloom Filter REJECTED shortCode: {}. Short-circuiting request.", shortCode);
            // Throwing your shiny new RFC 7807 exception!
            throw new UrlNotFoundException(UrlErrorCode.URL_NOT_FOUND);
        }

        String cacheUrl = urlCacheService.get(shortCode);

        if (cacheUrl != null) {
            repository.incrementClickCount(shortCode);
            log.debug("Cache HIT for shortCode: {}", shortCode);
            return cacheUrl;
        }

        log.debug("Cache MISS for shortCode: {}. Querying primary database.", shortCode);
        UrlMapping urlMapping = repository
                .findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Redirection failed: Short URL not found for code: {}", shortCode);
                    // Replaced string with Enum
                    return new UrlNotFoundException(UrlErrorCode.URL_NOT_FOUND);
                });

        if (urlMapping.getDeletedAt() != null) {
            log.warn("Redirection failed: Short URL is soft-deleted for code: {}", shortCode);
            // Replaced string with Enum
            throw new UrlNotFoundException(UrlErrorCode.URL_DELETED);
        }

        if (urlMapping.getExpiresAt() != null && LocalDateTime.now().isAfter(urlMapping.getExpiresAt())) {
            log.warn("Redirection failed: Short URL has expired for code: {}", shortCode);
            // Replaced string with Enum
            throw new ExpiredUrlException(UrlErrorCode.URL_EXPIRED);
        }

        repository.incrementClickCount(shortCode);
        urlCacheService.save(shortCode, urlMapping.getOriginalUrl(), urlMapping.getExpiresAt());

        log.debug("Successfully retrieved and cached original URL for shortCode: {}", shortCode);
        return urlMapping.getOriginalUrl();
    }

    @Override
    public void softDelete(String shortCode) {
        log.debug("Initiating soft delete for shortCode: {}", shortCode);

        UrlMapping urlMapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Soft delete failed: URL not found for code: {}", shortCode);
                    // Upgraded RuntimeException to custom Exception with Enum
                    return new UrlNotFoundException(UrlErrorCode.URL_NOT_FOUND);
                });

        if (urlMapping.getDeletedAt() != null) {
            log.warn("Soft delete skipped: URL already deleted for code: {}", shortCode);
            // Upgraded RuntimeException to custom Exception with Enum
            throw new UrlNotFoundException(UrlErrorCode.URL_DELETED);
        }

        urlMapping.setDeletedAt(LocalDateTime.now());
        repository.save(urlMapping);
        urlCacheService.delete(shortCode);

        log.info("Successfully soft-deleted shortCode: {}", shortCode);
    }

    @Override
    @Transactional
    public void restoreUrl(String shortCode) {
        log.debug("Initiating restoration for shortCode: {}", shortCode);

        UrlMapping url = repository.findByShortCode(shortCode)
                .orElseThrow(() -> {
                    log.warn("Restoration failed: URL not found for code: {}", shortCode);
                    // Replaced string with Enum
                    return new UrlNotFoundException(UrlErrorCode.URL_NOT_FOUND);
                });

        if (url.getDeletedAt() == null) {
            log.warn("Restoration skipped: URL is not currently deleted for code: {}", shortCode);
            // Replaced string with Enum
            throw new InvalidUrlException(UrlErrorCode.INVALID_URL);
        }

        url.setDeletedAt(null);
        repository.save(url);
        urlCacheService.save(shortCode, url.getOriginalUrl(), url.getExpiresAt());

        log.info("Successfully restored shortCode: {}", shortCode);
    }

    @Override
    public Page<UrlResponseDto> getAllUrls(Pageable pageable) {
        log.debug("Fetching all URLs with pagination and sorting");

        // Create a new Pageable instance that enforces sorting by createdAt descending
        Pageable sortedByCreatedAtDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("id").descending() // Or Sort.by("id").descending() if your IDs are chronological
        );

        return repository.findAll(sortedByCreatedAtDesc).map(this::mapToDto);
    }

    @Override
    public Page<UrlResponseDto> getUrlsReadyForHardDelete(Pageable pageable) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(deletedRetentionDays);

        log.debug("Fetching soft-deleted URLs older than {}", cutoffDate);

        // Enforce descending sorting by the deletion date so the oldest deleted links appear first
        Pageable sortedByDeletedAtDesc = PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                Sort.by("deletedAt").descending()
        );

        return repository
                .findByDeletedAtNotNull(sortedByDeletedAtDesc)
                .map(this::mapToDto);
    }

    private UrlResponseDto mapToDto(UrlMapping urlMapping) {
        String shortUrl = baseUrl + "/" + urlMapping.getShortCode();
        return UrlResponseDto.builder()
                .originalUrl(urlMapping.getOriginalUrl())
                .shortCode(urlMapping.getShortCode())
                .shortUrl(shortUrl)
                .build();
    }
}