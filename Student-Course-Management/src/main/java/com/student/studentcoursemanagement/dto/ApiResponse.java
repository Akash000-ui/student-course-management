package com.student.studentcoursemanagement.dto;

import java.time.LocalDateTime;

import lombok.Data;

@Data
public class ApiResponse<T>{

    private boolean success;
    private String message;
    private T data;
    private int statusCode;
    private LocalDateTime timestamp;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now();
    }

    public ApiResponse(boolean success, String message) {
        this();
        this.success = success;
        this.message = message;
    }

    public ApiResponse(boolean success, String message, T data) {
        this(success, message);
        this.data = data;
    }

    public ApiResponse(boolean success, String message, T data, int statusCode) {
        this(success, message, data);
        this.statusCode = statusCode;
    }


    public static <T> ApiResponse<T> success(String message) {
        return new ApiResponse<>(true, message);
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>(true, message, data);
    }

    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message);
    }


}
