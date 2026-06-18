package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.ProductReview;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ProductReviewRepository extends JpaRepository<ProductReview, Long> {

    List<ProductReview> findByProductIdOrderByReviewDateDesc(Long productId);

    // Kiểm tra user đã review sản phẩm trong đơn hàng này chưa (tránh đánh giá trùng)
    boolean existsByProductIdAndUserIdAndOrderId(Long productId, Long userId, Long orderId);
}
