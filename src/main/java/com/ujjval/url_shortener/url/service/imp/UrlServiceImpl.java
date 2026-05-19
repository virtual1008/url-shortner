package com.ujjval.url_shortener.url.service.impl;

import com.ujjval.url_shortener.common.util.Base62Encoder;
import com.ujjval.url_shortener.exception.ExpiredUrlException;
import com.ujjval.url_shortener.exception.UrlNotFoundException;
import com.ujjval.url_shortener.idgenerator.context.IdGenerationContext;
import com.ujjval.url_shortener.url.dto.UrlRequestDto;
import com.ujjval.url_shortener.url.dto.UrlResponseDto;
import com.ujjval.url_shortener.url.entity.UrlMapping;
import com.ujjval.url_shortener.url.repository.UrlMappingRepository;
import com.ujjval.url_shortener.url.service.UrlService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class UrlServiceImpl implements UrlService {

    private final UrlMappingRepository repository;

    private final IdGenerationContext idGenerationContext;

    @Value("${application.base-url}")
    private String baseUrl;

    @Value("${application.retention.default-years}")
    private long defaultRetentionYears;

    @Transactional
    @Override
    public UrlResponseDto shortenUrl(UrlRequestDto requestDto) {

        long id = idGenerationContext.generateId();

        UrlMapping urlMapping = new UrlMapping();

        urlMapping.setId(id);

        urlMapping.setOriginalUrl(requestDto.getOriginalUrl());

        String shortCode = Base62Encoder.encode(id);

        urlMapping.setShortCode(shortCode);

        LocalDateTime expiresAt;
        if(requestDto.getExpiresInSeconds()!=null){
            expiresAt = LocalDateTime.now().plusSeconds(requestDto.getExpiresInSeconds());
        }else{
            expiresAt = LocalDateTime.now().plusYears(defaultRetentionYears);
        }
        urlMapping.setExpiresAt(expiresAt);
        repository.save(urlMapping);

        String shortUrl = baseUrl + "/" + shortCode;

        return UrlResponseDto.builder()
                .originalUrl(urlMapping.getOriginalUrl())
                .shortCode(shortCode)
                .shortUrl(shortUrl)
                .build();
    }

    @Override
    public String getOriginalUrl(String shortCode) {
        UrlMapping urlMapping = repository
                .findByShortCode(shortCode)
                .orElseThrow(() ->
                        new UrlNotFoundException("Short URL not found")
                );
        if (urlMapping.getDeletedAt() != null) {
            throw new UrlNotFoundException("Short URL has been deleted");
        }
        if (urlMapping.getExpiresAt() != null &&
                LocalDateTime.now().isAfter(urlMapping.getExpiresAt())) {
            throw new ExpiredUrlException("Short URL has expired");
        }
        return urlMapping.getOriginalUrl();
    }
    @Override
    public void softDelete(String shortCode) {

        UrlMapping urlMapping = repository.findByShortCode(shortCode)
                .orElseThrow(() -> new RuntimeException("URL not found"));

        if (urlMapping.getDeletedAt() != null) {
            throw new RuntimeException("URL already deleted");
        }

        urlMapping.setDeletedAt(LocalDateTime.now());

        repository.save(urlMapping);
    }
}