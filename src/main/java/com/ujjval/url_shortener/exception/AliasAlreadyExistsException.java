package com.ujjval.url_shortener.exception;

public class AliasAlreadyExistsException extends BaseAppException{
    public AliasAlreadyExistsException(ErrorCode errorCode) {
        super(errorCode);
    }
}
