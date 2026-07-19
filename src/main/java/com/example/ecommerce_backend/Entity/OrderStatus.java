package com.example.ecommerce_backend.Entity;

public enum OrderStatus {
    PENDING,
    SHIPPING,
    DELIVERED,
    CANCELLED,
    RETURN_REQUESTED, // Khách hàng bấm yêu cầu đổi size
    RETURN_APPROVED,  // Quản lý đã duyệt yêu cầu đổi
    RETURNED          // Đã hoàn tất đổi trả hàng
}
