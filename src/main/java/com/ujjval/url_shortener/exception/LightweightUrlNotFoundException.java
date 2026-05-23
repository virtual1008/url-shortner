package com.ujjval.url_shortener.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class LightweightUrlNotFoundException extends BaseAppException{
       public LightweightUrlNotFoundException(ErrorCode errorCode){
           super(errorCode);
       }

    // THIS IS THE MAGIC METHOD
    // It prevents Java from doing the expensive work of building a stack trace
    @Override
    public synchronized Throwable fillInStackTrace() {
        return this;
    }
}
