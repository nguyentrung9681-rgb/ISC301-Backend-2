package com.example.ecommerce_backend.dto;

/**
 * Chuẩn hóa toàn bộ response API theo định dạng thống nhất:
 * { "success": true/false, "message": "...", "data": {...} }
 */
public class ApiResponse<T> {

    private boolean success;
    private String message;
    private T data;

    // Constructor thành công có data
    public static <T> ApiResponse<T> ok(T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.success = true;
        res.message = "Thành công";
        res.data = data;
        return res;
    }

    // Constructor thành công có message tuỳ chỉnh
    public static <T> ApiResponse<T> ok(String message, T data) {
        ApiResponse<T> res = new ApiResponse<>();
        res.success = true;
        res.message = message;
        res.data = data;
        return res;
    }

    // Constructor lỗi (không có data)
    public static <T> ApiResponse<T> error(String message) {
        ApiResponse<T> res = new ApiResponse<>();
        res.success = false;
        res.message = message;
        res.data = null;
        return res;
    }

    public boolean isSuccess() { return success; }
    public String getMessage() { return message; }
    public T getData() { return data; }
}
