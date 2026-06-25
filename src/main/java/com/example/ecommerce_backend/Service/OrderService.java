package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.*;
import com.example.ecommerce_backend.Repository.CartItemRepository;
import com.example.ecommerce_backend.Repository.OrderRepository;
import com.example.ecommerce_backend.Repository.ProductRepository;
import com.example.ecommerce_backend.Repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {
    @Autowired
    private OrderRepository orderRepository;
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;
    @Autowired
    private PaymentService paymentService;
    @Autowired
    private VoucherRepository voucherRepository;
    @Autowired
    private VoucherService voucherService;
    @Autowired
    private EmailService emailService;

    @Transactional
    public Order createOrder(User user, String address, String phone, String paymentMethod) {
        List<CartItem> cartItems = cartItemRepository.findByUserIdAndWishlist(user.getId(), false);
        if (cartItems.isEmpty())
            throw new RuntimeException("Giỏ hàng của bạn đang trống!");

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(address);
        order.setPhoneNumber(phone);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PENDING"); // Trạng thái khởi tạo mặc định

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal totalAmount = BigDecimal.ZERO;

        for (CartItem cart : cartItems) {
            Product product = cart.getProduct();
            // Kiểm tra kho vật lý
            if (product.getStockQuantity() < cart.getQuantity()) {
                throw new RuntimeException("Sản phẩm '" + product.getProductName() + "' không đủ số lượng trong kho!");
            }
            // Trừ số lượng kho vật lý
            product.setStockQuantity(product.getStockQuantity() - cart.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem(null, order, product, cart.getQuantity(), product.getPrice());
            orderItems.add(item);
            totalAmount = totalAmount.add(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        order.setTotalAmount(totalAmount);
        order.setOrderItems(orderItems);

        Order savedOrder = orderRepository.save(order);
        paymentService.createPayment(savedOrder);
        // Xóa sạch giỏ hàng sau khi checkout thành công
        cartItemRepository.deleteByUserIdAndIsWishlist(user.getId(), false);

        // Gửi email xác nhận đơn hàng mới (PENDING)
        try {
            emailService.sendOrderPendingEmail(user.getEmail(), savedOrder);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email xác nhận đặt hàng: " + e.getMessage());
        }

        return savedOrder;
    }

    @Transactional
    public Order cancelOrder(Long orderId, User user, boolean isManager) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        // Nếu là khách hàng, chỉ được phép hủy khi đơn ở trạng thái PENDING
        if (!isManager && !order.getStatus().equals("PENDING")) {
            throw new RuntimeException("Bạn chỉ có thể hủy đơn hàng khi trạng thái là PENDING!");
        }

        if (order.getStatus().equals("CANCELLED") || order.getStatus().equals("DELIVERED")) {
            throw new RuntimeException("Không thể hủy đơn hàng đã hoàn thành hoặc đã hủy trước đó.");
        }

        // Thực hiện Stock Rollback - Hoàn trả số lượng về kho hàng
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setStockQuantity(product.getStockQuantity() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus("CANCELLED");
        Order cancelledOrder = orderRepository.save(order);
        // Cập nhật trạng thái thanh toán sang REFUNDED khi đơn bị hủy
        paymentService.refundPayment(orderId);

        // Gửi email báo hủy đơn hàng
        try {
            emailService.sendOrderCancelledEmail(cancelledOrder.getUser().getEmail(), cancelledOrder);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email báo hủy đơn hàng: " + e.getMessage());
        }

        return cancelledOrder;
    }

    // 3. Manager cập nhật trạng thái đơn hàng (PENDING => SHIPPING => DELIVERED)
    public Order updateOrderStatus(Long orderId, String newStatus) {
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new RuntimeException("Đơn hàng không tồn tại"));

        if (order.getStatus().equals("CANCELLED")) {
            throw new RuntimeException("Không thể cập nhật trạng thái cho đơn hàng đã hủy!");
        }
        String oldStatus = order.getStatus();
        String formattedStatus = newStatus.toUpperCase();
        order.setStatus(formattedStatus);
        Order savedOrder = orderRepository.save(order);

        // Gửi email cập nhật trạng thái nếu chuyển sang SHIPPING hoặc DELIVERED
        if (!oldStatus.equalsIgnoreCase(formattedStatus) &&
            (formattedStatus.equals("SHIPPING") || formattedStatus.equals("DELIVERED"))) {
            try {
                emailService.sendOrderStatusUpdateEmail(savedOrder.getUser().getEmail(), savedOrder, formattedStatus);
            } catch (Exception e) {
                System.err.println("Lỗi gửi email cập nhật trạng thái đơn hàng: " + e.getMessage());
            }
        }

        return savedOrder;
    }

    // 4. Xem danh sách đơn hàng
    public List<Order> getAllOrders() {
        return orderRepository.findAll();
    }

    public List<Order> getClientOrderHistory(Long userId) {
        return orderRepository.findByUserIdOrderByOrderDateDesc(userId);
    }

    // VOUCHER
    @Transactional
    public Order createOrderWithVoucher(User user, String address, String phone, String paymentMethod,
            String voucherCode) {
        // 1. Kiểm tra và lấy thông tin Voucher hợp lệ (tái dùng logic trong
        // VoucherService)
        Voucher voucher = voucherService.validateVoucher(voucherCode);

        // 2. Gom sản phẩm từ giỏ hàng (tương tự hàm checkout cũ)
        List<CartItem> cartItems = cartItemRepository.findByUserIdAndWishlist(user.getId(), false);
        if (cartItems.isEmpty())
            throw new RuntimeException("Giỏ hàng rỗng!");

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setShippingAddress(address);
        order.setPhoneNumber(phone);
        order.setPaymentMethod(paymentMethod);
        order.setStatus("PENDING"); //

        List<OrderItem> orderItems = new ArrayList<>();
        BigDecimal subTotal = BigDecimal.ZERO;

        for (CartItem cart : cartItems) {
            Product product = cart.getProduct();
            if (product.getStockQuantity() < cart.getQuantity()) { //
                throw new RuntimeException("Sản phẩm '" + product.getProductName() + "' không đủ hàng trong kho!");
            }
            product.setStockQuantity(product.getStockQuantity() - cart.getQuantity());
            productRepository.save(product);

            OrderItem item = new OrderItem(null, order, product, cart.getQuantity(), product.getPrice());
            orderItems.add(item);
            subTotal = subTotal.add(product.getPrice().multiply(BigDecimal.valueOf(cart.getQuantity())));
        }

        // 3. Tính toán số tiền sau giảm giá
        BigDecimal discountRate = BigDecimal.valueOf(voucher.getDiscountPercent()).divide(BigDecimal.valueOf(100));
        BigDecimal discountAmount = subTotal.multiply(discountRate);
        BigDecimal finalAmount = subTotal.subtract(discountAmount);
        if (finalAmount.compareTo(BigDecimal.ZERO) < 0)
            finalAmount = BigDecimal.ZERO;

        order.setTotalAmount(finalAmount);
        order.setOrderItems(orderItems);

        // 4. Tăng số lượt đã sử dụng của voucher
        voucher.setUsedCount(voucher.getUsedCount() + 1);
        voucherRepository.save(voucher);

        Order savedOrder = orderRepository.save(order);
        paymentService.createPayment(savedOrder); // Tự động tạo bản ghi thanh toán kèm theo

        cartItemRepository.deleteByUserIdAndIsWishlist(user.getId(), false);

        // Gửi email xác nhận đơn hàng mới (PENDING)
        try {
            emailService.sendOrderPendingEmail(user.getEmail(), savedOrder);
        } catch (Exception e) {
            System.err.println("Lỗi gửi email xác nhận đặt hàng: " + e.getMessage());
        }

        return savedOrder;
    }
}
