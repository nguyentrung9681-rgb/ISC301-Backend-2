package com.example.ecommerce_backend.Config;

import com.example.ecommerce_backend.dto.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // Tài khoản bị BANNED → 403 Forbidden
    @ExceptionHandler(IllegalStateException.class)
    public ResponseEntity<ApiResponse<Void>> handleBannedAccount(IllegalStateException ex) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(ApiResponse.error(ex.getMessage()));
    }

    // Lỗi validation @Valid → 400 Bad Request kèm danh sách field lỗi
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleValidationException(MethodArgumentNotValidException ex) {
        String errors = ex.getBindingResult().getFieldErrors()
                .stream()
                .map(fe -> fe.getField() + ": " + fe.getDefaultMessage())
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest()
                .body(ApiResponse.error("Dữ liệu không hợp lệ: " + errors));
    }

    // RuntimeException tổng quát — phân loại theo message
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiResponse<Void>> handleRuntimeException(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage().toLowerCase() : "";

        if (msg.contains("không chính xác") || msg.contains("bị khóa") || msg.contains("banned")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error(ex.getMessage()));
        }
        if (msg.contains("không tìm thấy") || msg.contains("not found") || msg.contains("không tồn tại")) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(ApiResponse.error(ex.getMessage()));
        }
        if (msg.contains("đã tồn tại") || msg.contains("đã được đăng ký") || msg.contains("không đủ")) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body(ApiResponse.error(ex.getMessage()));
        }

        return ResponseEntity.badRequest()
                .body(ApiResponse.error(ex.getMessage()));
    }
}

