package com.ujjval.url_shortener.url.service;

import com.ujjval.url_shortener.url.dto.UrlRequestDto;
import com.ujjval.url_shortener.url.dto.UrlResponseDto;

public interface UrlService {

    UrlResponseDto shortenUrl(UrlRequestDto requestDto);

    String getOriginalUrl(String shortCode);
    void softDelete(String shortCode);
}