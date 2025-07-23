package com.example.common.exception;

public class OperationNotSupportException extends BaseException {

    public OperationNotSupportException(String message) {
        super("OPERATION_NOT_SUPPORT", message);
    }

}
