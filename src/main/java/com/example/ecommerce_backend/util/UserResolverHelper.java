package com.example.ecommerce_backend.util;

import com.example.ecommerce_backend.Entity.AccountStatus;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.stereotype.Component;

/**
 * Helper dùng chung để resolve User hiện tại từ header X-User-Id.
 * Thay thế getMockUser() và tránh duplicate code giữa các Controller.
 *
 * Cách dùng: inject vào Controller rồi gọi resolveCurrentUser(request)
 * - Trả về null nếu header thiếu, userId không hợp lệ, hoặc user không tồn tại
 * - Ném IllegalStateException nếu tài khoản bị BANNED
 */
@Component
public class UserResolverHelper {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public UserResolverHelper(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Resolve user từ header Authorization (Bearer JWT) hoặc header X-User-Id (fallback).
     *
     * @param request HTTP request chứa thông tin xác thực
     * @return User entity hoặc null nếu không xác thực được
     * @throws IllegalStateException nếu tài khoản bị BANNED
     */
    public User resolveCurrentUser(HttpServletRequest request) {
        // 1. Kiểm tra header Authorization trước (JWT Bearer Token)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    if (userId != null) {
                        User user = userRepository.findById(userId).orElse(null);
                        // Kiểm tra tài khoản bị khóa
                        if (user != null && user.getAccountStatus() == AccountStatus.BANNED) {
                            throw new IllegalStateException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
                        }
                        if (user != null) {
                            return user;
                        }
                    }
                }
            } catch (IllegalStateException e) {
                throw e; // Ném tiếp ngoại lệ tài khoản bị khóa
            } catch (Exception e) {
                // Token lỗi hoặc hết hạn, fallback xuống kiểm tra X-User-Id
            }
        }

        // 2. Fallback kiểm tra X-User-Id (hỗ trợ tương thích ngược)
        String userIdHeader = request.getHeader("X-User-Id");
        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            return null;
        }
        try {
            Long userId = Long.parseLong(userIdHeader.trim());
            User user = userRepository.findById(userId).orElse(null);

            // Kiểm tra tài khoản bị khóa
            if (user != null && user.getAccountStatus() == AccountStatus.BANNED) {
                throw new IllegalStateException("Tài khoản của bạn đã bị khóa. Vui lòng liên hệ quản trị viên.");
            }

            return user;
        } catch (NumberFormatException e) {
            return null;
        }
    }
}
