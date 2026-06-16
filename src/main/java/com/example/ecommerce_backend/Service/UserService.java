package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.dto.UserResponseDTO;
import com.example.ecommerce_backend.Entity.AccountStatus;
import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
@Service
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    // View user: Manager xem toàn bộ khách hàng
    public List<UserResponseDTO> getAllBuyers() {
        return userRepository.findByRole(Role.BUYER)
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    // Search user: Manager tìm khách hàng theo email hoặc số điện thoại
    public List<UserResponseDTO> searchBuyers(String keyword) {
        if (keyword == null || keyword.trim().isEmpty()) {
            return getAllBuyers();
        }

        return userRepository.searchUsersByEmailOrPhone(keyword.trim(), Role.BUYER)
                .stream()
                .map(UserResponseDTO::new)
                .toList();
    }

    // Update user status: ACTIVE hoặc BANNED
    public UserResponseDTO updateUserStatus(Long userId, AccountStatus status) {
        if (status == null) {
            throw new RuntimeException("Account status cannot be null");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found with id: " + userId));

        if (user.getRole() != Role.BUYER) {
            throw new RuntimeException("Only buyer accounts can be updated in User Management");
        }

        user.setAccountStatus(status);
        User updatedUser = userRepository.save(user);

        return new UserResponseDTO(updatedUser);
    }
}
