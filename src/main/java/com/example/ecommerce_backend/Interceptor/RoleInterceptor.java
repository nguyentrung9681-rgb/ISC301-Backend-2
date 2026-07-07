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

        // 4. Kiểm tra phân quyền: Phân chia chi tiết giữa STAFF và MANAGER
        String path = request.getServletPath();
        String method = request.getMethod();

        if (user.getRole() == Role.MANAGER) {
            return true;
        }

        if (user.getRole() == Role.STAFF) {
            boolean isAllowed = false;

            // 1. Quản lý sản phẩm: STAFF được quyền (Xem, Thêm, Sửa, Ẩn sản phẩm, Upload ảnh) trừ duyệt/từ chối sản phẩm
            if (path.startsWith("/api/manager/products")) {
                if (path.endsWith("/approve") || path.endsWith("/reject")) {
                    isAllowed = false;
                } else {
                    isAllowed = true;
                }
            }
            // 2. Quản lý danh mục: STAFF chỉ được xem danh mục (GET)
            else if (path.startsWith("/api/manager/categories") && "GET".equalsIgnoreCase(method)) {
                isAllowed = true;
            }
            // 3. Quản lý mã giảm giá: STAFF chỉ được xem danh sách mã giảm giá (GET)
            else if (path.startsWith("/api/manager/voucher/list") && "GET".equalsIgnoreCase(method)) {
                isAllowed = true;
            }
            // 4. Quản lý Buyer: STAFF chỉ được xem danh sách hoặc tìm kiếm Buyer (GET)
            else if (path.startsWith("/api/manager/users") && "GET".equalsIgnoreCase(method)) {
                // Bảo vệ các endpoint không dành cho STAFF: tạo manager, tạo staff, đổi status
                if (!path.contains("/create-manager") && !path.contains("/create-staff") && !path.contains("/status")) {
                    isAllowed = true;
                }
            }
            // 5. Quản lý đơn hàng: STAFF được xem đơn hàng (GET) và cập nhật trạng thái đơn (PUT)
            else if (path.startsWith("/api/admin/orders")) {
                if ("GET".equalsIgnoreCase(method)) {
                    isAllowed = true;
                } else if ("PUT".equalsIgnoreCase(method) && path.matches("^/api/admin/orders/\\d+/status$")) {
                    isAllowed = true;
                }
            }

            if (isAllowed) {
                return true;
            }
        }

        // Không đủ quyền truy cập
        response.setStatus(HttpServletResponse.SC_FORBIDDEN); // 403 Forbidden
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write("{\"success\":false,\"message\":\"Quyền truy cập bị từ chối: Bạn không có quyền thực hiện chức năng này\",\"data\":null}");
        return false;
    }
}
