package com.example.ecommerce_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne //1 don hang <-> 1 ban thanh toan duy nhat
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    private BigDecimal amount;

    @Column(name = "payment_method", length = 255)
    private String paymentMethod; //"COD", "BANK_TRANSFER", "VNPAY"
    private String status; //"PENDING", "PAID", "FAILED", "REFUNDED"
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt; // Thời điểm cập nhật trạng thái (PAID / REFUNDED)

    //Payos
    private Long payosOrderCode;
    @Column(length = 1000)
    private String checkoutUrl;//link thanh toán trả về
}
