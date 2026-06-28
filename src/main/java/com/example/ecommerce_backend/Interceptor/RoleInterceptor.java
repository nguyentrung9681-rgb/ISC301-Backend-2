package com.example.ecommerce_backend.Interceptor;

import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Repository.UserRepository;
import com.example.ecommerce_backend.util.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class RoleInterceptor implements HandlerInterceptor {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public RoleInterceptor(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // Hỗ trợ Preflight request của CORS (nếu là phương thức OPTIONS thì cho qua luôn)
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        User user = null;

        // 1. Kiểm tra header Authorization trước (JWT Bearer Token)
        String authHeader = request.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String token = authHeader.substring(7).trim();
            try {
                if (jwtUtil.validateToken(token)) {
                    Long userId = jwtUtil.getUserIdFromToken(token);
                    if (userId != null) {
                        user = userRepository.findById(userId).orElse(null);
                    }
                }
            } catch (Exception e) {
                // Token lỗi hoặc hết hạn, fallback xuống kiểm tra X-User-Id
            }
        }

        // 2. Fallback kiểm tra X-User-Id cũ nếu chưa xác định được User
        if (user == null) {
            String userIdHeader = request.getHeader("X-User-Id");
            if (userIdHeader != null && !userIdHeader.trim().isEmpty()) {
                try {
                    Long userId = Long.parseLong(userIdHeader.trim());
                    user = userRepository.findById(userId).orElse(null);
                } catch (NumberFormatException e) {
                    response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
                    response.setContentType("application/json");
                    response.setCharacterEncoding("UTF-8");
                    response.getWriter().write("{\"success\":false,\"message\":\"Mã định danh người dùng không hợp lệ\",\"data\":null}");
                    return false;
                }
            }
        }

        // 3. Nếu vẫn không xác thực được User thì chặn đứng request
        if (user == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 401 Unauthorized
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Yêu cầu đăng nhập để truy cập\",\"data\":null}");
            return false; // Ngăn chặn request tiếp tục đi vào Controller
        }

        // 4. Kiểm tra phân quyền: Nếu không phải MANAGER thì chặn đứng request
        if (user.getRole() != Role.MANAGER) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write("{\"success\":false,\"message\":\"Quyền truy cập bị từ chối: Chỉ dành cho Admin/Manager\",\"data\":null}");
            return false;
        }

        // Hợp lệ, cho phép request đi tiếp vào Controller gác cổng quản trị
        return true;
    }
}
