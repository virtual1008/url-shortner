package com.ujjval.url_shortener.exception;

public class ExpiredUrlException extends RuntimeException {

    public ExpiredUrlException(String message) {
        super(message);
    }

    public ExpiredUrlException(String message, Throwable cause) {
        super(message, cause);
    }
}