package com.ujjval.url_shortener.exception;

public class ExpiredUrlException extends BaseAppException {

    public ExpiredUrlException(ErrorCode errorCode) {
        super(errorCode);
    }
}