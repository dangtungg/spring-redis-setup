package com.example.common.response;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ErrorResponse<T> {
    private String errorCode;
    private String message;
    private T errors;
    private LocalDateTime timestamp;
    private String path;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class ValidationError {
        private String field;
        private String message;
    }

    public static <T> ErrorResponse<T> of(String errorCode, String message) {
        ErrorResponse<T> response = new ErrorResponse<>();
        response.setErrorCode(errorCode);
        response.setMessage(message);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> ErrorResponse<T> of(String errorCode, String message, String path) {
        ErrorResponse<T> response = of(errorCode, message);
        response.setPath(path);
        return response;
    }

    public static <T> ErrorResponse<T> of(String errorCode, String message, T errors) {
        ErrorResponse<T> response = of(errorCode, message);
        response.setErrors(errors);
        return response;
    }

    public static <T> ErrorResponse<T> of(String errorCode, String message, T errors, String path) {
        ErrorResponse<T> response = of(errorCode, message);
        response.setErrors(errors);
        response.setPath(path);
        return response;
    }

}
