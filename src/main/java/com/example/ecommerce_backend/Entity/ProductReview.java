package com.example.ecommerce_backend.Entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "product_reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Min(value = 1, message = "Rating tối thiểu là 1 sao")
    @Max(value = 5, message = "Rating tối đa là 5 sao")
    private int rating; // 1 -> 5 sao

    @Column(length = 1000)
    private String comment;

    private LocalDateTime reviewDate;
    private Long orderId; // Đối chiếu xác nhận khách đã thực sự mua hàng

    @PrePersist
    protected void onCreate() {
        if (this.reviewDate == null) {
            this.reviewDate = LocalDateTime.now();
        }
    }
}
