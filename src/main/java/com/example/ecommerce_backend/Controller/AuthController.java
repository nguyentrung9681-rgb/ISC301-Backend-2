package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.UserService;
import com.example.ecommerce_backend.dto.*;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "http://localhost:3000") // Cho phép kết nối từ Front-end của bạn
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    // API Đăng ký người dùng mới
    // POST: http://localhost:8080/api/auth/register
    @PostMapping("/register")
    public UserResponseDTO register(@RequestBody RegisterRequestDTO request) {
        return userService.registerUser(request);
    }

    // 1. API Đăng nhập
// POST: http://localhost:8080/api/auth/login
    @PostMapping("/login")
    public UserResponseDTO login(@RequestBody LoginRequestDTO request) {
        return userService.loginUser(request);
    }

    // 2. API Đăng xuất
// POST: http://localhost:8080/api/auth/logout
    @PostMapping("/logout")
    public String logout() {
        // Vì hiện tại hệ thống là Stateless (hoặc xử lý lưu Token/Session ở Client)
        // Phía FE chỉ cần xóa dữ liệu User/Token trong localStorage/sessionStorage khi gọi API này.
        return "Đăng xuất thành công";
    }
    //3.API quen mat khau
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(@RequestBody ForgotPasswordRequestDTO request) {
        try {
            userService.generatePasswordResetToken(request.getEmail());
            return ResponseEntity.ok().body("{\"message\": \"Hệ thống đã gửi một liên kết đặt lại mật khẩu tới email của bạn. Vui lòng kiểm tra hòm thư!\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // API 4: Thực hiện đổi và cập nhật lại Mật khẩu mới
    @PostMapping("/reset-password")
    public ResponseEntity<?> resetPassword(@RequestBody ResetPasswordRequestDTO request) {
        try {
            userService.resetPassword(request.getToken(), request.getNewPassword());
            return ResponseEntity.ok().body("{\"message\": \"Mật khẩu của bạn đã được thay đổi và cập nhật thành công!\"}");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    // API Đăng nhập bằng Google
    // POST: http://localhost:8080/api/auth/google
    @PostMapping("/google")
    public ResponseEntity<?> loginGoogle(@RequestBody GoogleLoginRequestDTO request) {
        try {
            UserResponseDTO response = userService.loginGoogle(request);
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
