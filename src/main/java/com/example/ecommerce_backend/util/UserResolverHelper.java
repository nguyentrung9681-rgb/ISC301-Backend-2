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

    public UserResolverHelper(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Resolve user từ header X-User-Id.
     *
     * @param request HTTP request chứa header X-User-Id
     * @return User entity hoặc null nếu không xác thực được
     * @throws IllegalStateException nếu tài khoản bị BANNED
     */
    public User resolveCurrentUser(HttpServletRequest request) {
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
