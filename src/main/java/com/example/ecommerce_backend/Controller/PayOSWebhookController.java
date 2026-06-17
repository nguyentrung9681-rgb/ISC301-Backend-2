package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import vn.payos.PayOS;
import vn.payos.model.webhooks.WebhookData;

@RestController
@RequestMapping("/api/payment")
public class PayOSWebhookController {

    @Autowired
    private PayOS payOS;

    @Autowired
    private PaymentService paymentService;

    // POST /api/payment/webhook -> Điểm tiếp nhận dữ liệu báo đã thanh toán từ server PayOS
    @PostMapping("/webhook")
    public ResponseEntity<?> receiveWebhook(@RequestBody Object webhookBody) {
        try {
            // 1. Sử dụng SDK PayOS để kiểm tra tính toàn vẹn và xác thực chữ ký (Tránh việc hacker fake request)
            WebhookData verifiedData = payOS.webhooks().verify(webhookBody);

            // 2. Nếu verify thành công, đọc mã hóa đơn đối chiếu và số tiền
            Long payosOrderCode = verifiedData.getOrderCode();

            // 3. Tiến hành cập nhật trạng thái giao dịch nội bộ thành PAID
            paymentService.updateStatusByPayOSCode(payosOrderCode, "PAID");

            // 4. Phản hồi lại cho PayOS biết hệ thống của bạn đã xử lý thành công thông điệp
            return ResponseEntity.ok().body("{\"message\": \"Webhook processed successfully\"}");
        } catch (Exception e) {
            // Nếu chữ ký sai hoặc có lỗi, trả về mã lỗi để PayOS gửi lại sau
            return ResponseEntity.badRequest().body("{\"error\": \"" + e.getMessage() + "\"}");
        }
    }
}
