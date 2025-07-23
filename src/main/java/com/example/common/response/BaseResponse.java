package com.example.common.response;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
public class BaseResponse<T> {
    private String code;
    private String message;
    private T data;
    private LocalDateTime timestamp;

    public static <T> BaseResponse<T> success(T data) {
        BaseResponse<T> response = new BaseResponse<>();
        response.setCode("SUCCESS");
        response.setMessage("Operation completed successfully");
        response.setData(data);
        response.setTimestamp(LocalDateTime.now());
        return response;
    }

    public static <T> BaseResponse<T> success(T data, String message) {
        BaseResponse<T> response = success(data);
        response.setMessage(message);
        return response;
    }
}
