package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.UserService;
import com.example.ecommerce_backend.dto.RegisterRequestDTO;
import com.example.ecommerce_backend.dto.UserResponseDTO;
import org.springframework.web.bind.annotation.*;
import com.example.ecommerce_backend.dto.LoginRequestDTO;

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
}
