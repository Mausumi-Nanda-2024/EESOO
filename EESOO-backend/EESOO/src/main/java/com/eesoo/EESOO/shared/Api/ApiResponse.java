package com.eesoo.EESOO.shared.Api;

public class ApiResponse<T> {

    private String status;
    private String message;
    private T data;

    //  Private constructor so we use static factory methods
    private ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    //  Static method for success responses
    public static <T> ApiResponse<T> success(String message, T data) {
        return new ApiResponse<>("success", message, data);
    }

    //  Static method for error/failure responses
    public static <T> ApiResponse<T> error(String message, T data) {
        return new ApiResponse<>("error", message, data);
    }

    //  Getters — required for JSON serialization
    public String getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }
    
}
