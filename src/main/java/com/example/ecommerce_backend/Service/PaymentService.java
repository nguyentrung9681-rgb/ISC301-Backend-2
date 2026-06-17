package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Order;
import com.example.ecommerce_backend.Entity.Payment;
import com.example.ecommerce_backend.Repository.PaymentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import vn.payos.PayOS;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkRequest;
import vn.payos.model.v2.paymentRequests.CreatePaymentLinkResponse;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class PaymentService {
    @Autowired
    private PaymentRepository paymentRepository;

    @Autowired
    private PayOS payOS;
    @Value("${payos.return-url}")
    private String returnUrl;

    @Value("${payos.cancel-url}")
    private String cancelUrl;

    // Tự động tạo payment khi đặt hàng thành công (status: PENDING)
    public Payment createPayment(Order order) {
        Payment payment = new Payment();
        payment.setOrder(order);
        payment.setAmount(order.getTotalAmount());
        payment.setPaymentMethod(order.getPaymentMethod());
        payment.setStatus("PENDING");
        payment.setCreatedAt(LocalDateTime.now());
        return paymentRepository.save(payment);
    }

    // Xác nhận thanh toán thành công -> chuyển sang PAID
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin thanh toán"));
        if ("PAID".equals(payment.getStatus())) {
            throw new RuntimeException("Giao dịch này đã được thanh toán");
        }
        payment.setStatus("PAID");
        payment.setUpdatedAt(LocalDateTime.now()); // Ghi nhận thời điểm xác nhận
        return paymentRepository.save(payment);
    }

    // Hoàn tiền khi đơn hàng bị hủy -> chuyển sang REFUNDED
    public void refundPayment(Long orderId) {
        paymentRepository.findByOrderId(orderId).ifPresent(payment -> {
            payment.setStatus("REFUNDED");
            payment.setUpdatedAt(LocalDateTime.now()); // Ghi nhận thời điểm hoàn tiền
            paymentRepository.save(payment);
        });
    }

    // Xem thông tin thanh toán theo đơn hàng
    public Payment getPaymentByOrder(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy dữ liệu thanh toán"));
    }

    // Xem toàn bộ lịch sử thanh toán của một user
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByOrderUserId(userId);
    }

    //PAYOS
    @Transactional
    public String createPayOSPayment(Long orderId) {

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()-> new RuntimeException("Không tìm thấy thông tin thanh toán của đơn hàng này"));

        Long payosOrderCode = System.currentTimeMillis() / 1000;
        Long totalAmount = payment.getAmount().longValue();

        CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                .orderCode(payosOrderCode)
                .amount(totalAmount)
                .description("Thanh toan don hang #" + orderId)
                .returnUrl(returnUrl)
                .cancelUrl(cancelUrl)
                .build();
        try {
            // Gọi API chính thức sang hệ thống PayOS
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentRequest);

            // Cập nhật thông tin mã PayOS và Link QR vừa nhận được vào DB hệ thống
            payment.setPayosOrderCode(payosOrderCode);
            payment.setCheckoutUrl(response.getCheckoutUrl());
            payment.setPaymentMethod("PAYOS");
            paymentRepository.save(payment);

            // Trả link QR về cho Controller gửi tiếp cho Frontend
            return response.getCheckoutUrl();
        } catch (Exception e) {
            throw new RuntimeException("Lỗi trong quá trình kết nối cổng thanh toán PayOS: " + e.getMessage());
        }
    }

    @Transactional
    public void updateStatusByPayOSCode(Long payosOrderCode, String status) {
        Payment payment = paymentRepository.findByPayosOrderCode(payosOrderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã đơn giao dịch PayOS đối chiếu!"));
        payment.setStatus(status.toUpperCase());
        paymentRepository.save(payment);
    }

}
