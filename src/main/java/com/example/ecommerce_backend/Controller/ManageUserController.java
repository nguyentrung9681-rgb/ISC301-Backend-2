package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Service.UserService;
import com.example.ecommerce_backend.dto.RegisterManagerRequestDTO;
import com.example.ecommerce_backend.dto.UpdateUserStatusRequest;
import com.example.ecommerce_backend.dto.UserResponseDTO;
import com.example.ecommerce_backend.dto.UpdateUserRoleRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/manager/users")
public class ManageUserController {
    private final UserService userService;

    public ManageUserController(UserService userService) {
        this.userService = userService;
    }

    // View user
    // GET: http://localhost:8080/api/manager/users
    @GetMapping
    public List<UserResponseDTO> getAllBuyers() {
        return userService.getAllBuyers();
    }

    // Search user
    // GET: http://localhost:8080/api/manager/users/search?keyword=abc
    @GetMapping("/search")
    public List<UserResponseDTO> searchBuyers(@RequestParam String keyword) {
        return userService.searchBuyers(keyword);
    }

    // Update user status
    // PATCH: http://localhost:8080/api/manager/users/1/status
    @PatchMapping("/{id}/status")
    public UserResponseDTO updateUserStatus(
            @PathVariable Long id,
            @RequestBody UpdateUserStatusRequest request
    ) {
        return userService.updateUserStatus(id, request.getStatus());
    }

    // Update user role
    // PATCH: http://localhost:8080/api/manager/users/{id}/role
    @PatchMapping("/{id}/role")
    public ResponseEntity<?> updateUserRole(
            @PathVariable Long id,
            @RequestBody UpdateUserRoleRequest request
    ) {
        try {
            UserResponseDTO updatedUser = userService.updateUserRole(id, request.getRole());
            return ResponseEntity.ok(updatedUser);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    //post /api/manager/users/create-manager -> nhận request khởi tạo tài khoản manager
    @PostMapping("/create-manager")
    public ResponseEntity<?> createManager(@RequestBody RegisterManagerRequestDTO request) {
        try {
            UserResponseDTO savedManager = userService.registerManager(request);
            return ResponseEntity.ok(savedManager);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

    //post /api/manager/users/create-staff -> nhận request khởi tạo tài khoản staff
    @PostMapping("/create-staff")
    public ResponseEntity<?> createStaff(@RequestBody RegisterManagerRequestDTO request) {
        try {
            UserResponseDTO savedStaff = userService.registerStaff(request);
            return ResponseEntity.ok(savedStaff);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }

}
