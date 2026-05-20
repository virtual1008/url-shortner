package com.ujjval.url_shortener.exception;

public class InvalidUrlException extends BaseAppException {

    public InvalidUrlException(ErrorCode errorCode) {
        super(errorCode);
    }
}