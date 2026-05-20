package com.ujjval.url_shortener.exception;

import lombok.Getter;

@Getter
public abstract class BaseAppException extends RuntimeException{
    private final ErrorCode errorCode;

    public BaseAppException(ErrorCode errorCode){
        super(errorCode.getCode());
        this.errorCode = errorCode;
    }
}
