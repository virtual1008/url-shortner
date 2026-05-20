package com.ujjval.url_shortener.exception;

public class UrlNotFoundException extends BaseAppException {

    public UrlNotFoundException(ErrorCode errorCode) {
        super(errorCode);
    }
}