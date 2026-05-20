package com.ujjval.url_shortener.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum UrlErrorCode implements ErrorCode{

    URL_NOT_FOUND("URL-1001", "error.url.not_found", HttpStatus.NOT_FOUND),
    URL_EXPIRED("URL-1002", "error.url.expired", HttpStatus.GONE),
    ALIAS_EXISTS("URL-1003", "error.alias.exists", HttpStatus.CONFLICT),
    URL_DELETED("URL-1004", "error.url.deleted", HttpStatus.GONE),
    INVALID_URL("URL-1005", "error.url.invalid", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String messageKey;
    private final HttpStatus status;
}
