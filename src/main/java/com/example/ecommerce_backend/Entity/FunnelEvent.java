package com.example.ecommerce_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "funnel_events")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class FunnelEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "event_type", length = 50, nullable = false)
    private String eventType; // VIEW_PRODUCT, ADD_TO_CART, INITIATE_CHECKOUT, PURCHASE

    @Column(name = "session_id", length = 100, nullable = false)
    private String sessionId;

    @Column(name = "product_id")
    private Long productId;

    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;

    @PrePersist
    protected void onCreate() {
        if (this.timestamp == null) {
            this.timestamp = LocalDateTime.now();
        }
    }
}
