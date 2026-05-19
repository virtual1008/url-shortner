package com.ujjval.url_shortener.url.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UrlResponseDto {

    private final String originalUrl;

    private final String shortUrl;

    private final String shortCode;
}