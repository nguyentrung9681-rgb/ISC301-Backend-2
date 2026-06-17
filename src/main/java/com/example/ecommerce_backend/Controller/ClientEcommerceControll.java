package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.*;
import com.example.ecommerce_backend.Service.CartService;
import com.example.ecommerce_backend.Service.OrderService;
import com.example.ecommerce_backend.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.example.ecommerce_backend.Entity.AccountStatus.ACTIVE;

@RestController
@RequestMapping("api/client")
public class ClientEcommerceControll {

    @Autowired
    private CartService cartService;
    @Autowired
    private OrderService orderService;
    @Autowired
    private PaymentService paymentService;

    private User getMockUser() {
        return new User(1L, "khachhang@gmail.com", "", "Nguyen Van A", "0987654321", "Ha Noi", Role.BUYER, ACTIVE);
    }
    //GIO HANG + WISHLIST
    @GetMapping("/cart")
    public ResponseEntity<List<CartItem>> getCart() {
        return ResponseEntity.ok(cartService.getItems(getMockUser(), false));
    }

    @PostMapping("/cart/add")
    public ResponseEntity<CartItem> addToCart(@RequestParam Long productId, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.addItem(getMockUser(), productId, quantity, false));
    }

    @PutMapping("/cart/update/{id}")
    public ResponseEntity<CartItem> updateCartQuantity(@PathVariable Long id, @RequestParam int quantity) {
        return ResponseEntity.ok(cartService.updateQuantity(id, quantity));
    }

    @DeleteMapping("/cart/delete/{id}")
    public ResponseEntity<String> deleteCartItem(@PathVariable Long id) {
        cartService.deleteItem(id);
        return ResponseEntity.ok("Đã xóa mục khỏi giỏ hàng!");
    }

    @GetMapping("/wishlist")
    public ResponseEntity<List<CartItem>> getWishlist() {
        return ResponseEntity.ok(cartService.getItems(getMockUser(), true));
    }

    @PostMapping("/wishlist/add")
    public ResponseEntity<CartItem> addToWishlist(@RequestParam Long productId) {
        return ResponseEntity.ok(cartService.addItem(getMockUser(), productId, 1, true));
    }

    //DON HANG CLIENT
    @PostMapping("/order/checkout")
    public ResponseEntity<Order> checkout(@RequestParam String address, @RequestParam String phone, @RequestParam String paymentMethod) {
        return ResponseEntity.ok(orderService.createOrder(getMockUser(), address, phone, paymentMethod));
    }

    @PostMapping("/order/cancel/{id}")
    public ResponseEntity<Order> clientCancelOrder(@PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id, getMockUser(), false));
    }

    @GetMapping("/order/history")
    public ResponseEntity<List<Order>> getHistory() {
        return ResponseEntity.ok(orderService.getClientOrderHistory(getMockUser().getId()));
    }

    //THANH TOAN
    @GetMapping("/payment/{orderId}")
    public ResponseEntity<Payment> getPaymentInfo(@PathVariable Long orderId) {
        return ResponseEntity.ok(paymentService.getPaymentByOrder(orderId));
    }

    @PostMapping("/payment/confirm/{paymentId}")
    public ResponseEntity<Payment> confirmPayment(@PathVariable Long paymentId) {
        return ResponseEntity.ok(paymentService.confirmPayment(paymentId));
    }

    @GetMapping("/payment/history")
    public ResponseEntity<List<Payment>> getPaymentHistory() {
        return ResponseEntity.ok(paymentService.getPaymentsByUser(getMockUser().getId()));
    }

    //PAYOS
    @PostMapping("/payment/payos/{orderId}")
    public ResponseEntity<?> generatePayOSLink(@PathVariable Long orderId) {
        String checkoutUrl = paymentService.createPayOSPayment(orderId);
        // Trả về dạng JSON chứa link để Frontend tự động redirect sang trang quét mã
        return ResponseEntity.ok().body("{\"checkoutUrl\": \"" + checkoutUrl + "\"}");
    }

}
