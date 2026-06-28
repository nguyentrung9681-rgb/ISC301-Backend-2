package com.example.ecommerce_backend.config;

import com.example.ecommerce_backend.Interceptor.RoleInterceptor; // Thêm import này
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 1. Khai báo thuộc tính RoleInterceptor
    private final RoleInterceptor roleInterceptor;

    // 2. Tiêm (Inject) RoleInterceptor thông qua Constructor
    public WebConfig(RoleInterceptor roleInterceptor) {
        this.roleInterceptor = roleInterceptor;
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**") // Áp dụng cấu hình cho tất cả các URL API
                .allowedOrigins("http://localhost:3000") // Chỉ cho phép nguồn từ ReactJS port 3000 gọi tới
                .allowedMethods("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS") // Cho phép các phương thức HTTP cơ bản
                .allowedHeaders("*") // Cho phép tất cả các loại Headers mà Axios gửi lên
                .allowCredentials(true) // Cho phép gửi kèm Cookies hoặc thông tin xác thực
                .maxAge(3600); // Thời gian trình duyệt được phép lưu cache kết quả Preflight request
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(roleInterceptor)
                .addPathPatterns("/api/manager/**", "/api/admin/**"); // Áp dụng bộ lọc cho các đường dẫn quản trị viên và admin
    }
}
