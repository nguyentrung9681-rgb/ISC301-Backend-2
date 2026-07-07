package com.example.ecommerce_backend.Entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "vouchers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Voucher {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false, length = 50)
    private String code;

    @Column(nullable = false)
    private Integer discountPercent; // 1 - 100 (%)

    @Column(nullable = false)
    private Integer maxUses;         // số lượt dùng tối đa

    private Integer usedCount = 0;   // số lượt đã dùng (mặc định 0)

    private LocalDateTime expiryDate; // null = không hết hạn

    // Dùng tên "active" thay vì "isActive" để Lombok sinh getActive()/setActive()
    // tránh xung đột với JSON serialization (Jackson expect "active" key)
    @Column(nullable = false)
    private Boolean active = true;
}
