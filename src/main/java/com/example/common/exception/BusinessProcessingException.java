package com.example.common.exception;

public class BusinessProcessingException extends BaseException {

    public BusinessProcessingException(String message) {
        super("BUSINESS_PROCESSING_FAILED", message);
    }

}
