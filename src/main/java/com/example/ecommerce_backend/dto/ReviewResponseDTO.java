package com.example.ecommerce_backend.dto;

import com.example.ecommerce_backend.Entity.ProductReview;

import java.time.LocalDateTime;

/**
 * DTO cho response trả về phía client khi xem đánh giá.
 * Chỉ expose các field cần thiết, tránh vòng lặp JSON và lộ dữ liệu nhạy cảm.
 */
public class ReviewResponseDTO {

    private Long id;
    private Long productId;
    private String productName;
    private Long userId;
    private String userName;
    private int rating;
    private String comment;
    private LocalDateTime reviewDate;
    private Long orderId;

    // --- Static factory từ Entity ---
    public static ReviewResponseDTO fromEntity(ProductReview review) {
        ReviewResponseDTO dto = new ReviewResponseDTO();
        dto.id          = review.getId();
        dto.productId   = review.getProduct().getId();
        dto.productName = review.getProduct().getProductName();
        dto.userId      = review.getUser().getId();
        dto.userName    = review.getUser().getFullName();
        dto.rating      = review.getRating();
        dto.comment     = review.getComment();
        dto.reviewDate  = review.getReviewDate();
        dto.orderId     = review.getOrderId();
        return dto;
    }

    // --- Getters ---

    public Long getId() { return id; }
    public Long getProductId() { return productId; }
    public String getProductName() { return productName; }
    public Long getUserId() { return userId; }
    public String getUserName() { return userName; }
    public int getRating() { return rating; }
    public String getComment() { return comment; }
    public LocalDateTime getReviewDate() { return reviewDate; }
    public Long getOrderId() { return orderId; }
}
