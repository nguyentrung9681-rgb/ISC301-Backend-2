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

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:}")
    private String fromEmail;

    // ========== EMAIL TEST ENDPOINT ==========

    @GetMapping("/test-email")
    public ResponseEntity<?> testEmail(@RequestParam String to) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            if (fromEmail != null && !fromEmail.isEmpty()) {
                message.setFrom(fromEmail);
            }
            message.setTo(to);
            message.setSubject("[JustLife] Test Email Connection");
            message.setText("Đây là email kiểm tra kết nối SMTP từ hệ thống JustLife!");
            mailSender.send(message);
            return ResponseEntity.ok("Email gửi thành công! Hãy kiểm tra hòm thư của bạn.");
        } catch (Exception e) {
            java.io.StringWriter sw = new java.io.StringWriter();
            e.printStackTrace(new java.io.PrintWriter(sw));
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Gửi email thất bại! Chi tiết lỗi:\n" + e.getMessage() + "\n\nStacktrace:\n" + sw.toString());
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
    public ResponseEntity<ApiResponse<?>> addToCart(@RequestParam Long productId, @RequestParam int quantity, HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(ApiResponse.error("Yêu cầu đăng nhập"));
        return ResponseEntity.ok(ApiResponse.ok(cartService.addItem(currentUser, productId, quantity, false)));
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
        return ResponseEntity.ok(ApiResponse.ok(cartService.addItem(currentUser, productId, 1, true)));
    }

    // ========== ĐƠN HÀNG CLIENT ==========

    @PostMapping("/order/checkout")
    public ResponseEntity<ApiResponse<?>> checkout(
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

        if (addressId != null) {
            UserAddress userAddress = userAddressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với ID: " + addressId));
            if (!userAddress.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Địa chỉ không thuộc về tài khoản của bạn!"));
            }
            finalAddress = userAddress.getFullName() + ", " + userAddress.getAddressDetail();
            finalPhone = userAddress.getPhone();
        } else {
            if (finalAddress == null || finalAddress.trim().isEmpty() || finalPhone == null || finalPhone.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Vui lòng cung cấp địa chỉ nhận hàng và số điện thoại!"));
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(orderService.createOrder(currentUser, finalAddress, finalPhone, paymentMethod)));
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

        if (addressId != null) {
            UserAddress userAddress = userAddressRepository.findById(addressId)
                    .orElseThrow(() -> new RuntimeException("Địa chỉ không tồn tại với ID: " + addressId));
            if (!userAddress.getUser().getId().equals(currentUser.getId())) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Địa chỉ không thuộc về tài khoản của bạn!"));
            }
            finalAddress = userAddress.getFullName() + ", " + userAddress.getAddressDetail();
            finalPhone = userAddress.getPhone();
        } else {
            if (finalAddress == null || finalAddress.trim().isEmpty() || finalPhone == null || finalPhone.trim().isEmpty()) {
                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                        .body(ApiResponse.error("Vui lòng cung cấp địa chỉ nhận hàng và số điện thoại!"));
            }
        }

        return ResponseEntity.ok(ApiResponse.ok(orderService.createOrderWithVoucher(currentUser, finalAddress, finalPhone, paymentMethod, voucherCode)));
    }
}