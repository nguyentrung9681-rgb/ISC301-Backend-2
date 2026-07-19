package com.example.ecommerce_backend.Entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "orders")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Order {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    private LocalDateTime orderDate;
    private BigDecimal totalAmount;
    @Column(name = "shipping_address", length = 255)
    private String shippingAddress;
    private String phoneNumber;

    @Column(name = "receiver_name", length = 255)
    private String receiverName;

    @Column(name = "payment_method", length = 255)
    private String paymentMethod;

    @Column(name = "status", length = 255)
    private String status; // PENDING -> SHIPPING -> DELIVERED / CANCELLED | RETURN_REQUESTED -> RETURN_APPROVED -> RETURNED

    @JsonManagedReference
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL)
    private List<OrderItem> orderItems;
}
