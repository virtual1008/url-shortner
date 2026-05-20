package com.ujjval.url_shortener.exception;

import org.springframework.http.HttpStatus;

public interface ErrorCode {
    String getCode();
    String getMessageKey();
    HttpStatus getStatus();
}
