package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Order;
import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductReview;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Repository.OrderRepository;
import com.example.ecommerce_backend.Repository.ProductRepository;
import com.example.ecommerce_backend.Repository.ProductReviewRepository;
import com.example.ecommerce_backend.dto.ReviewRequestDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Service
public class ReviewService {

    @Autowired
    private ProductReviewRepository productReviewRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private OrderRepository orderRepository;

    // 1. Thêm đánh giá mới + Tự động tính toán lại ratingAverage của sản phẩm
    @Transactional
    public ProductReview addReview(ReviewRequestDTO dto, User currentUser) {
        // Kiểm tra đơn hàng tồn tại và thuộc về user hiện tại
        Order order = orderRepository.findById(dto.getOrderId())
                .filter(o -> o.getUser().getId().equals(currentUser.getId()))
                .orElseThrow(() -> new RuntimeException("Đơn hàng không hợp lệ hoặc bạn chưa mua sản phẩm này!"));

        // Chỉ cho phép đánh giá khi đơn hàng ở trạng thái DELIVERED
        if (!"DELIVERED".equalsIgnoreCase(order.getStatus())) {
            throw new RuntimeException("Bạn chỉ có thể đánh giá sản phẩm sau khi đơn hàng đã được giao thành công!");
        }

        // Kiểm tra sản phẩm tồn tại
        Product product = productRepository.findById(dto.getProductId())
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        // Kiểm tra user đã đánh giá đơn hàng + sản phẩm này chưa (tránh đánh giá trùng)
        boolean alreadyReviewed = productReviewRepository
                .existsByProductIdAndUserIdAndOrderId(dto.getProductId(), currentUser.getId(), dto.getOrderId());
        if (alreadyReviewed) {
            throw new RuntimeException("Bạn đã đánh giá sản phẩm này trong đơn hàng này rồi!");
        }

        // Tạo entity từ DTO
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setUser(currentUser);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
        review.setOrderId(dto.getOrderId());
        // reviewDate sẽ được set tự động qua @PrePersist trong entity

        ProductReview savedReview = productReviewRepository.save(review);

        // Tính lại ratingAverage bằng BigDecimal đúng cách
        List<ProductReview> allReviews = productReviewRepository.findByProductIdOrderByReviewDateDesc(product.getId());

        BigDecimal sum = BigDecimal.ZERO;
        for (ProductReview pr : allReviews) {
            sum = sum.add(BigDecimal.valueOf(pr.getRating()));
        }
        // Chia và làm tròn 1 chữ số thập phân (ví dụ: 4.6666 -> 4.7)
        BigDecimal average = sum.divide(BigDecimal.valueOf(allReviews.size()), 1, RoundingMode.HALF_UP);

        product.setRatingAverage(average);
        productRepository.save(product);

        return savedReview;
    }

    // 2. Xem tất cả các bài đánh giá của một sản phẩm (Public)
    public List<ProductReview> getReviewsByProduct(Long productId) {
        return productReviewRepository.findByProductIdOrderByReviewDateDesc(productId);
    }

    // 3. Manager xóa bài đánh giá tiêu cực hoặc vi phạm tiêu chuẩn chính sách
    @Transactional
    public void deleteReview(Long id) {
        ProductReview review = productReviewRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Bài đánh giá không tồn tại!"));

        Long productId = review.getProduct().getId();
        productReviewRepository.deleteById(id);

        // Tính lại ratingAverage sau khi xóa
        List<ProductReview> remaining = productReviewRepository.findByProductIdOrderByReviewDateDesc(productId);
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("Sản phẩm không tồn tại!"));

        if (remaining.isEmpty()) {
            // Khi không còn review nào, reset về giá trị mặc định
            product.setRatingAverage(BigDecimal.valueOf(5.0));
        } else {
            BigDecimal sum = BigDecimal.ZERO;
            for (ProductReview pr : remaining) {
                sum = sum.add(BigDecimal.valueOf(pr.getRating()));
            }
            BigDecimal average = sum.divide(BigDecimal.valueOf(remaining.size()), 1, RoundingMode.HALF_UP);
            product.setRatingAverage(average);
        }
        productRepository.save(product);
    }
}
