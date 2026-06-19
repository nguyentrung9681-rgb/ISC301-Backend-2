package com.example.ecommerce_backend.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", 400);
        body.put("error", ex.getMessage());

        // Phân loại lỗi theo message để trả đúng HTTP status
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (msg.contains("không chính xác") || msg.contains("bị khóa") || msg.contains("banned")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(body);
        }
        if (msg.contains("không tìm thấy") || msg.contains("not found") || msg.contains("không tồn tại")) {
            body.put("status", 404);
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
        }
        if (msg.contains("đã tồn tại") || msg.contains("đã được đăng ký") || msg.contains("không đủ")) {
            body.put("status", 409);
            return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
        }

        return ResponseEntity.badRequest().body(body);
    }
}
