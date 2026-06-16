package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.UserService;
import com.example.ecommerce_backend.dto.UpdateUserStatusRequest;
import com.example.ecommerce_backend.dto.UserResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@RequestMapping("/api/manager/users")
@CrossOrigin(origins = "http://localhost:3000")
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
}
