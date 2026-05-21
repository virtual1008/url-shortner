package com.ujjval.url_shortener.exception;

import com.ujjval.url_shortener.exception.dto.ErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Locale;

@RestControllerAdvice
@RequiredArgsConstructor
public class GlobalExceptionHandler {

    private final MessageSource messageSource;

    @ExceptionHandler(BaseAppException.class)
    public ResponseEntity<ProblemDetail> handleAppException(BaseAppException ex , Locale locale){
           ErrorCode errorCode = ex.getErrorCode();

           String detailMessage = messageSource.getMessage(errorCode.getMessageKey(),null , locale);
           ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(errorCode.getStatus(), detailMessage);
           problemDetail.setTitle(errorCode.getCode());
           problemDetail.setType(URI.create("https://api.yourdomain.com/errors/" + errorCode.getCode()));
           problemDetail.setProperty("errorCode", errorCode.getCode());
           problemDetail.setProperty("timestamp", Instant.now());

           return ResponseEntity.status(errorCode.getStatus()).body(problemDetail);
    }
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetail> handleValidationException(MethodArgumentNotValidException ex) {

        String defaultMessage = ex.getBindingResult()
                .getFieldError() != null ?
                ex.getBindingResult().getFieldError().getDefaultMessage() : "Validation failed";

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, defaultMessage);
        problemDetail.setTitle("Validation Failed");
        problemDetail.setType(URI.create("https://api.yourdomain.com/errors/VALIDATION-FAILED"));
        problemDetail.setProperty("errorCode", "VALIDATION-1000");
        problemDetail.setProperty("timestamp", Instant.now());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(problemDetail);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetail> handleGenericException(Exception ex) {

        ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "An unexpected error occurred. Please try again later."
        );
        problemDetail.setTitle("Internal Server Error");
        problemDetail.setType(URI.create("https://api.yourdomain.com/errors/INTERNAL-SERVER-ERROR"));
        problemDetail.setProperty("errorCode", "SYS-5000");
        problemDetail.setProperty("timestamp", Instant.now());


        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(problemDetail);
    }


}