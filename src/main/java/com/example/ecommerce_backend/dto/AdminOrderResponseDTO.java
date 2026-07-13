package com.example.ecommerce_backend.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record AdminOrderResponseDTO(
        Long id,
        String customerName,
        String phone,
        String address,
        BigDecimal totalAmount,
        String paymentMethod,
        String status,
        String paymentStatus,
        LocalDateTime orderDate,
        List<AdminOrderItemResponseDTO> orderItems
) {
    public record AdminOrderItemResponseDTO(
            Long id,
            Long productId,
            String productName,
            Integer quantity,
            BigDecimal price,
            String imageUrl
    ) {
    }
}
