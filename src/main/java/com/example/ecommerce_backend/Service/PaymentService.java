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
    private QRCodeService qrCodeService;
    @Autowired
    private EmailService emailService;

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
    @Transactional
    public Payment confirmPayment(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy thông tin giao dịch thanh toán!"));

        if ("PAID".equals(payment.getStatus())) {
            throw new RuntimeException("Giao dịch này đã được thanh toán trước đó.");
        }

        payment.setStatus("PAID");
        Payment savedPayment = paymentRepository.save(payment);

        // 🔥 KÍCH HOẠT TỰ ĐỘNG GỬI BILL KÈM QR
        triggerOrderInvoiceEmail(savedPayment.getOrder());

        return savedPayment;
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

    // Lấy trạng thái thanh toán an toàn theo đơn hàng
    public String getPaymentStatusByOrderId(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .map(Payment::getStatus)
                .orElse("PENDING");
    }

    // Xem toàn bộ lịch sử thanh toán của một user
    public List<Payment> getPaymentsByUser(Long userId) {
        return paymentRepository.findByOrderUserId(userId);
    }

    //PAYOS
    @Transactional
    public java.util.Map<String, String> createPayOSPayment(Long orderId, String originUrl) {

        Payment payment = paymentRepository.findByOrderId(orderId).orElseThrow(()-> new RuntimeException("Không tìm thấy thông tin thanh toán của đơn hàng này"));

        Long payosOrderCode = System.currentTimeMillis() / 1000;
        Long totalAmount = payment.getAmount().longValue();

        String finalReturnUrl = returnUrl;
        String finalCancelUrl = cancelUrl;
        if (originUrl != null && !originUrl.trim().isEmpty()) {
            String base = originUrl.trim();
            if (base.endsWith("/")) {
                base = base.substring(0, base.length() - 1);
            }
            finalReturnUrl = base + "/payment/success";
            finalCancelUrl = base + "/payment/cancel";
        }

        CreatePaymentLinkRequest paymentRequest = CreatePaymentLinkRequest.builder()
                .orderCode(payosOrderCode)
                .amount(totalAmount)
                .description("Thanh toan don hang #" + orderId)
                .returnUrl(finalReturnUrl)
                .cancelUrl(finalCancelUrl)
                .build();
        try {
            // Gọi API chính thức sang hệ thống PayOS
            CreatePaymentLinkResponse response = payOS.paymentRequests().create(paymentRequest);

            // Cập nhật thông tin mã PayOS và Link QR vừa nhận được vào DB hệ thống
            payment.setPayosOrderCode(payosOrderCode);
            payment.setCheckoutUrl(response.getCheckoutUrl());
            payment.setPaymentMethod("PAYOS");
            paymentRepository.save(payment);

            // Trả link QR và chuỗi VietQR về cho Controller gửi tiếp cho Frontend
            java.util.Map<String, String> result = new java.util.HashMap<>();
            result.put("checkoutUrl", response.getCheckoutUrl());
            result.put("qrCode", response.getQrCode());
            return result;
        } catch (Exception e) {
            throw new RuntimeException("Lỗi trong quá trình kết nối cổng thanh toán PayOS: " + e.getMessage());
        }
    }

    @Transactional
    public void updateStatusByPayOSCode(Long payosOrderCode, String status) {
        Payment payment = paymentRepository.findByPayosOrderCode(payosOrderCode)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy mã đơn giao dịch PayOS đối chiếu!"));

        if (!"PAID".equals(payment.getStatus()) && "PAID".equalsIgnoreCase(status)) {
            payment.setStatus("PAID");
            Payment savedPayment = paymentRepository.save(payment);

            // 🔥 KÍCH HOẠT TỰ ĐỘNG GỬI BILL KÈM QR
            triggerOrderInvoiceEmail(savedPayment.getOrder());
        }
    }

    // Hàm MOI: nội bộ giúp sinh mã QR và bắn Mail (Tránh viết lặp code)
    private void triggerOrderInvoiceEmail(Order order) {
        try {
            // Chuỗi text mã hóa vào QR (Ví dụ: Dẫn đến link tra cứu hoặc thông tin nhanh của đơn)
            String qrData = "JustLife-Order-ID: " + order.getId()
                    + " | Customer: " + order.getUser().getFullName()
                    + " | Total: " + order.getTotalAmount() + " VND";

            // Tạo ảnh QR kích thước 200x200 px
            byte[] qrImage = qrCodeService.generateQRCodeImage(qrData, 200, 200);

            // Gửi email hóa đơn trực tiếp cho khách hàng
            emailService.sendOrderConfirmationEmail(order.getUser().getEmail(), order, qrImage);
        } catch (Exception e) {
            // Log warning or print stack trace instead of rolling back the entire transaction!
            System.err.println("Lỗi khi gửi email hóa đơn cho đơn hàng #" + order.getId() + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

}
