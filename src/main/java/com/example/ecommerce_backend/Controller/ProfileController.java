package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.UserService;
import com.example.ecommerce_backend.dto.UserResponseDTO;
import org.springframework.web.bind.annotation.*;
import com.example.ecommerce_backend.dto.UpdateProfileRequestDTO;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000") // Cho phép Front-end React kết nối
public class ProfileController {

    private final UserService userService;

    public ProfileController(UserService userService) {
        this.userService = userService;
    }

    // API lấy thông tin cá nhân theo ID người dùng
    // GET: http://localhost:8080/api/profile/{id}
    @GetMapping("/{id}")
    public UserResponseDTO getProfile(@PathVariable Long id) {
        return userService.getUserProfile(id);
    }

    // Thêm API cập nhật thông tin cá nhân
// PUT: http://localhost:8080/api/profile/{id}
    @PutMapping("/{id}")
    public UserResponseDTO updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequestDTO request) {
        return userService.updateUserProfile(id, request);
    }
}