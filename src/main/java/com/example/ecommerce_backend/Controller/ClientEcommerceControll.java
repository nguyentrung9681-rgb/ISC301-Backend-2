package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.*;
import com.example.ecommerce_backend.Service.CartService;
import com.example.ecommerce_backend.Service.OrderService;
import com.example.ecommerce_backend.Service.PaymentService;
import com.example.ecommerce_backend.Service.VoucherService;
import com.example.ecommerce_backend.dto.ApiResponse;
import com.example.ecommerce_backend.util.UserResolverHelper;
import com.example.ecommerce_backend.Repository.UserAddressRepository;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.SimpleMailMessage;
import java.util.List;

@RestController
@RequestMapping("api/client")
public class ClientEcommerceControll {

    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private UserResolverHelper userResolverHelper;
    @Autowired
    private UserAddressRepository userAddressRepository;

    @Value("${sendgrid.api-key:}")
    private String sendGridApiKey;

    @Value("${sendgrid.from:}")
    private String sendGridFrom;

    // ========== EMAIL TEST ENDPOINT ==========

    @GetMapping({"/test-email", "/test_email"})
    public ResponseEntity<?> testEmail(@RequestParam String to) {
        try {
            if (sendGridApiKey == null || sendGridApiKey.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Gửi email thất bại! Chưa cấu hình SENDGRID_API_KEY (biến môi trường rỗng hoặc chưa cập nhật).");
            }
            if (sendGridFrom == null || sendGridFrom.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body("Gửi email thất bại! Chưa cấu hình SENDGRID_FROM_EMAIL (biến môi trường rỗng hoặc chưa cập nhật).");
            }

            org.springframework.web.client.RestTemplate restTemplate = new org.springframework.web.client.RestTemplate();
            org.springframework.http.HttpHeaders headers = new org.springframework.http.HttpHeaders();
            headers.setContentType(org.springframework.http.MediaType.APPLICATION_JSON);
            headers.setBearerAuth(sendGridApiKey);

            java.util.Map<String, Object> body = new java.util.HashMap<>();

            // personalizations
            java.util.Map<String, Object> personalization = new java.util.HashMap<>();
            java.util.Map<String, String> toRecipient = new java.util.HashMap<>();
            toRecipient.put("email", to);
            personalization.put("to", java.util.Collections.singletonList(toRecipient));
            personalization.put("subject", "[JustLife] Test Connection via SendGrid API");
            body.put("personalizations", java.util.Collections.singletonList(personalization));

            // from
            java.util.Map<String, String> fromSender = new java.util.HashMap<>();
            fromSender.put("email", sendGridFrom);
            body.put("from", fromSender);

            // content
            java.util.Map<String, String> contentObj = new java.util.HashMap<>();
            contentObj.put("type", "text/html");
            contentObj.put("value", "<p>Đây là email kiểm tra kết nối SendGrid HTTP API từ hệ thống JustLife!</p>");
            body.put("content", java.util.Collections.singletonList(contentObj));

            org.springframework.http.HttpEntity<java.util.Map<String, Object>> entity = new org.springframework.http.HttpEntity<>(body, headers);
            org.springframework.http.ResponseEntity<String> response = restTemplate.postForEntity("https://api.sendgrid.com/v3/mail/send", entity, String.class);

            return ResponseEntity.ok("Email gửi thành công qua SendGrid API! Mã phản hồi HTTP: " + response.getStatusCode());
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Gửi email qua SendGrid thất bại! Chi tiết lỗi:\n" + e.getMessage() + "\n\nStacktrace:\n" + sw.toString());
        }
    }

    // ========== GIỎ HÀNG ==========

    @GetMapping("/cart")
    public ResponseEntity<ApiResponse<?>> getCart(HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));
        return ResponseEntity.ok(ApiResponse.ok(cartService.getItems(currentUser, false)));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<ApiResponse<?>> addToCart(
            @RequestParam Long productId,
            @RequestParam int quantity,
            @RequestParam(required = false) String size,
            @RequestParam(required = false) String color,
            HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));
        return ResponseEntity.ok(ApiResponse.ok(cartService.addItem(currentUser, productId, quantity, size, color, false)));
    }

    @PutMapping("/cart/update/{id}")
    public ResponseEntity<ApiResponse<CartItem>> updateCartQuantity(@PathVariable Long id, @RequestParam int quantity) {
        return ResponseEntity.ok(ApiResponse.ok(cartService.updateQuantity(id, quantity)));
    }

    @DeleteMapping("/cart/delete/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteCartItem(@PathVariable Long id) {
        cartService.deleteItem(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã xóa mục khỏi giỏ hàng!", null));
    }

    // ========== WISHLIST ==========

    @GetMapping("/wishlist")
    public ResponseEntity<ApiResponse<?>> getWishlist(HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));
        return ResponseEntity.ok(ApiResponse.ok(cartService.getItems(currentUser, true)));
    }

    @PostMapping("/wishlist/add")
    public ResponseEntity<ApiResponse<?>> addToWishlist(@RequestParam Long productId, HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));
        return ResponseEntity.ok(ApiResponse.ok(cartService.addItem(currentUser, productId, 1, null, null, true)));
    }

    // ========== ĐƠN HÀNG CLIENT ==========

    @PostMapping("/order/checkout")
    public ResponseEntity<ApiResponse<?>> checkout(
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long addressId,
            @RequestParam String paymentMethod,
            HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));

        String finalAddress = address;
        String finalPhone = phone;
        String finalReceiverName = receiverName;

        if (addressId != null) {
            UserAddress userAddress = userAddressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với ID: " + addressId));
            if (!userAddress.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Địa chỉ không thuộc về tài khoản của bạn!"));
            }
            finalAddress = userAddress.getFullName() + ", " + userAddress.getAddressDetail();
            finalPhone = userAddress.getPhone();
            finalReceiverName = userAddress.getFullName();
        } else {
            if (finalAddress == null || finalAddress.trim().isEmpty() || finalPhone == null || finalPhone.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Vui lòng cung cấp địa chỉ nhận hàng và số điện thoại!"));
            }
        }

        if (finalReceiverName == null || finalReceiverName.trim().isEmpty()) {
            finalReceiverName = currentUser.getFullName();
        }

        return ResponseEntity.ok(ApiResponse.ok(orderService.createOrder(currentUser, finalReceiverName, finalAddress, finalPhone, paymentMethod)));
    }

    @PostMapping("/order/cancel/{id}")
    public ResponseEntity<ApiResponse<?>> clientCancelOrder(@PathVariable Long id, HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));
        return ResponseEntity.ok(ApiResponse.ok(orderService.cancelOrder(id, currentUser, false)));
    }

    @GetMapping("/order/history")
    public ResponseEntity<ApiResponse<?>> getHistory(HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));
        return ResponseEntity.ok(ApiResponse.ok(orderService.getClientOrderHistory(currentUser.getId())));
    }

    // ========== THANH TOÁN ==========

    @GetMapping("/payment/{orderId}")
    public ResponseEntity<ApiResponse<Payment>> getPaymentInfo(@PathVariable Long orderId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPaymentByOrder(orderId)));
    }

    @PostMapping("/payment/confirm/{paymentId}")
    public ResponseEntity<ApiResponse<Payment>> confirmPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(ApiResponse.ok(paymentService.confirmPayment(paymentId)));
    }

    @GetMapping("/payment/history")
    public ResponseEntity<ApiResponse<?>> getPaymentHistory(HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));
        return ResponseEntity.ok(ApiResponse.ok(paymentService.getPaymentsByUser(currentUser.getId())));
    }

    // ========== PAYOS ==========

    @PostMapping("/payment/payos/{orderId}")
    public ResponseEntity<ApiResponse<java.util.Map<String, String>>> generatePayOSLink(@PathVariable Long orderId, HttpServletRequest request) {
        String origin = request.getHeader("Origin");
        if (origin == null || origin.trim().isEmpty()) {
            origin = request.getHeader("Referer");
        }
        java.util.Map<String, String> paymentData = paymentService.createPayOSPayment(orderId, origin);
        return ResponseEntity.ok(ApiResponse.ok("Link thanh toán đã được tạo", paymentData));
    }

    // ========== VOUCHER ==========

    @GetMapping("/voucher/validate")
    public ResponseEntity<ApiResponse<Voucher>> checkVoucher(@RequestParam String code) {
        return ResponseEntity.ok(ApiResponse.ok(voucherService.validateVoucher(code)));
    }

    @GetMapping("/voucher/active")
    public ResponseEntity<ApiResponse<List<Voucher>>> getActiveVouchers() {
        return ResponseEntity.ok(ApiResponse.ok(voucherService.getActiveVouchersForClient()));
    }

    @PostMapping("/order/checkout-with-voucher")
    public ResponseEntity<ApiResponse<?>> checkoutWithVoucher(
            @RequestParam(required = false) String receiverName,
            @RequestParam(required = false) String address,
            @RequestParam(required = false) String phone,
            @RequestParam(required = false) Long addressId,
            @RequestParam String paymentMethod,
            @RequestParam String voucherCode,
            HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));

        String finalAddress = address;
        String finalPhone = phone;
        String finalReceiverName = receiverName;

        if (addressId != null) {
            UserAddress userAddress = userAddressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với ID: " + addressId));
            if (!userAddress.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Địa chỉ không thuộc về tài khoản của bạn!"));
            }
            finalAddress = userAddress.getFullName() + ", " + userAddress.getAddressDetail();
            finalPhone = userAddress.getPhone();
            finalReceiverName = userAddress.getFullName();
        } else {
            if (finalAddress == null || finalAddress.trim().isEmpty() || finalPhone == null || finalPhone.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Vui lòng cung cấp địa chỉ nhận hàng và số điện thoại!"));
            }
        }

        if (finalReceiverName == null || finalReceiverName.trim().isEmpty()) {
            finalReceiverName = currentUser.getFullName();
        }

        return ResponseEntity.ok(ApiResponse.ok(orderService.createOrderWithVoucher(currentUser, finalReceiverName, finalAddress, finalPhone, paymentMethod, voucherCode)));
    }
}