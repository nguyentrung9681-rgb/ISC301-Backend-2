package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.Order;
import com.example.ecommerce_backend.Service.OrderService;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.dto.AdminOrderResponseDTO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class ManagerOrderController {

    @Autowired
    private OrderService orderService;

    // Xem toàn bộ đơn hàng hiện có trên hệ thống
    @GetMapping
    public ResponseEntity<List<AdminOrderResponseDTO>> viewAllOrders() {
        return ResponseEntity.ok(orderService.getAllOrdersForAdmin());
    }

    // Tiếp nhận đơn hàng và cập nhật trạng thái (PENDING => SHIPPING => DELIVERED)
    @PutMapping("/{id}/status")
    public ResponseEntity<Order> updateStatus(@PathVariable Long id, @RequestParam String status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    // Manager chủ động hủy đơn bị lỗi + Kích hoạt tự động hoàn hàng về kho
    @PostMapping("/{id}/cancel")
    public ResponseEntity<Order> managerCancelOrder(@PathVariable Long id) {
        User managerUser = new User(); // Đại diện tài khoản Manager thực thi tác vụ
        return ResponseEntity.ok(orderService.cancelOrder(id, managerUser, true));
    }
}
