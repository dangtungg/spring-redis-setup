package com.example.common.exception;

import java.util.List;

public class RequestValidationException extends BaseException {

    public RequestValidationException(String message) {
        super("VALIDATION_FAILED", message);
    }

    public RequestValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public RequestValidationException(String message, List<String> errors) {
        super("VALIDATION_FAILED", message, errors);
    }

    public RequestValidationException(String errorCode, String message, List<String> errors) {
        super(errorCode, message, errors);
    }
}
