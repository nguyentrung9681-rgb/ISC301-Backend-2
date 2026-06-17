package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.Payment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PaymentRepository extends JpaRepository<Payment, Long> {
    // Tìm kiếm thông tin thanh toán theo mã đơn hàng
    Optional<Payment> findByOrderId(Long orderId);

    // Lấy toàn bộ lịch sử thanh toán của một user (thông qua Order -> User)
    List<Payment> findByOrderUserId(Long userId);

    Optional<Payment> findByPayosOrderCode(Long payosOrderCode);
}
