package com.example.common.exception;

import lombok.Getter;

import java.util.List;

@Getter
public class BaseException extends RuntimeException {
    private String errorCode;
    private List<String> errors;

    public BaseException(String message) {
        super(message);
    }

    public BaseException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, String message, List<String> errors) {
        super(message);
        this.errorCode = errorCode;
        this.errors = errors;
    }

    public BaseException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    public BaseException(String errorCode, String message, List<String> errors, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
        this.errors = errors;
    }
}
