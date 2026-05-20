package com.ujjval.url_shortener.url.service;

import com.ujjval.url_shortener.url.dto.UrlRequestDto;
import com.ujjval.url_shortener.url.dto.UrlResponseDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UrlService {

    UrlResponseDto shortenUrl(UrlRequestDto requestDto);

    String getOriginalUrl(String shortCode);
    void softDelete(String shortCode);
    void restoreUrl(String shortCode);
    Page<UrlResponseDto> getAllUrls(Pageable pageable);
    Page<UrlResponseDto> getUrlsReadyForHardDelete(Pageable pageable);
}