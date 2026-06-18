package com.example.ecommerce_backend.Interceptor;

import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;

    public RoleInterceptor(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Hỗ trợ Preflight request của CORS (nếu là phương thức OPTIONS thì cho qua luôn)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        // 1. Đọc UserId gửi kèm từ Header của request
        String userIdHeader = request.getHeader("X-User-Id");

        if (userIdHeader == null || userIdHeader.trim().isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            response.getWriter().write("Yeu cau dang nhap de truy cap");
            return false; // Ngăn chặn request tiếp tục đi vào Controller
        }

        try {
            Long userId = Long.parseLong(userIdHeader.trim());

            // 2. Tra cứu thông tin người dùng trong cơ sở dữ liệu
            Optional<User> userOpt = userRepository.findById(userId);
            if (userOpt.isEmpty()) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.getWriter().write("Nguoi dung khong ton tai tren he thong");
                return false;
            }

            User user = userOpt.get();

            // 3. Kiểm tra phân quyền: Nếu không phải MANAGER thì chặn đứng request
            if (user.getRole() != Role.MANAGER) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write("Quyen truy cap bi tu choi: Chi danh cho Admin/Manager");
                return false;
            }

            // Hợp lệ, cho phép request đi tiếp vào Controller gác cổng quản trị
            return true;

        } catch (NumberFormatException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().write("Ma dinh danh nguoi dung khong hop le");
            return false;
        }
    }
}
