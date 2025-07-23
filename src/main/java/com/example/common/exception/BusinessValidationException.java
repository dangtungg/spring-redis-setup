package com.example.common.exception;

import java.util.List;

public class BusinessValidationException extends BaseException {

    public BusinessValidationException(String message) {
        super("BUSINESS_VALIDATION_FAILED", message);
    }

    public BusinessValidationException(String errorCode, String message) {
        super(errorCode, message);
    }

    public BusinessValidationException(String message, List<String> errors) {
        super("BUSINESS_VALIDATION_FAILED", message, errors);
    }

}
