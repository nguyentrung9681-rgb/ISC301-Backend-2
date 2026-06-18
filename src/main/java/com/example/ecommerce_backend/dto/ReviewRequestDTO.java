package com.example.ecommerce_backend.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

/**
 * DTO cho request gửi đánh giá từ Client.
 * Tách biệt khỏi Entity để tránh expose toàn bộ dữ liệu nội bộ.
 */
public class ReviewRequestDTO {

    @NotNull(message = "productId không được để trống")
    private Long productId;

    @NotNull(message = "orderId không được để trống")
    private Long orderId;

    @Min(value = 1, message = "Rating tối thiểu là 1 sao")
    @Max(value = 5, message = "Rating tối đa là 5 sao")
    private int rating;

    private String comment;

    // --- Getters & Setters ---

    public Long getProductId() {
        return productId;
    }

    public void setProductId(Long productId) {
        this.productId = productId;
    }

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }
}
