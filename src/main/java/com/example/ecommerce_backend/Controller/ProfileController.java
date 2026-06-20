package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Service.UserService;
import com.example.ecommerce_backend.dto.ApiResponse;
import com.example.ecommerce_backend.dto.UpdateProfileRequestDTO;
import com.example.ecommerce_backend.dto.UserResponseDTO;
import com.example.ecommerce_backend.util.UserResolverHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/profile")
@CrossOrigin(origins = "http://localhost:3000")
public class ProfileController {

    private final UserService userService;
    private final UserResolverHelper userResolverHelper;

    public ProfileController(UserService userService, UserResolverHelper userResolverHelper) {
        this.userService = userService;
        this.userResolverHelper = userResolverHelper;
    }

    // GET /api/profile/me — Lấy thông tin cá nhân của user đang đăng nhập
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> getMyProfile(HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập"));
        }
        return ResponseEntity.ok(ApiResponse.ok(userService.getUserProfile(currentUser.getId())));
    }

    // PUT /api/profile/me — Cập nhật thông tin cá nhân của user đang đăng nhập
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserResponseDTO>> updateMyProfile(
            @RequestBody UpdateProfileRequestDTO request,
            HttpServletRequest httpRequest) {
        User currentUser = userResolverHelper.resolveCurrentUser(httpRequest);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập"));
        }
        UserResponseDTO updated = userService.updateUserProfile(currentUser.getId(), request);
        return ResponseEntity.ok(ApiResponse.ok("Cập nhật thông tin thành công", updated));
    }
}