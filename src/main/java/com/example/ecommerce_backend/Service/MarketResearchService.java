package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.FunnelEvent;
import com.example.ecommerce_backend.Repository.OrderItemRepository;
import com.example.ecommerce_backend.Repository.OrderRepository;
import com.example.ecommerce_backend.Repository.ProductRepository;
import com.example.ecommerce_backend.Repository.FunnelEventRepository;
import com.example.ecommerce_backend.dto.MarketResearchDTO.*;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


@Service
public class MarketResearchService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private FunnelEventRepository funnelEventRepository;

    // ---------------------------------------------------------------
    // 1. Thống kê doanh thu tổng hợp theo khoảng thời gian
    // ---------------------------------------------------------------
    public RevenueStatsDTO getRevenueStats(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);

        BigDecimal totalRevenue = orderRepository.getTotalRevenue(start, end);
        long totalOrders = orderRepository.countDeliveredOrders(start, end);

        BigDecimal averageOrderValue = BigDecimal.ZERO;
        if (totalOrders > 0) {
            averageOrderValue = totalRevenue.divide(BigDecimal.valueOf(totalOrders), 2, RoundingMode.HALF_UP);
        }

        // Chi tiết theo ngày
        List<Object[]> dailyRaw = orderRepository.getRevenueByDay(start, end);
        List<DailyRevenueDTO> breakdown = mapToDailyRevenue(dailyRaw);

        return new RevenueStatsDTO(
                totalRevenue,
                totalOrders,
                averageOrderValue,
                startDate.toString(),
                endDate.toString(),
                breakdown
        );
    }

    // ---------------------------------------------------------------
    // 2. Doanh thu theo từng tháng trong năm
    // ---------------------------------------------------------------
    public List<DailyRevenueDTO> getMonthlyRevenue(int year) {
        List<Object[]> raw = orderRepository.getRevenueByMonth(year);
        return mapToDailyRevenue(raw);
    }

    // ---------------------------------------------------------------
    // 3. Doanh thu theo từng ngày trong khoảng thời gian
    // ---------------------------------------------------------------
    public List<DailyRevenueDTO> getDailyRevenue(LocalDate startDate, LocalDate endDate) {
        LocalDateTime start = startDate.atStartOfDay();
        LocalDateTime end = endDate.atTime(LocalTime.MAX);
        List<Object[]> raw = orderRepository.getRevenueByDay(start, end);
        return mapToDailyRevenue(raw);
    }

    // ---------------------------------------------------------------
    // 4. Top sản phẩm bán chạy nhất
    // ---------------------------------------------------------------
    public List<TopProductDTO> getTopSellingProducts(int limit) {
        List<Object[]> raw = orderItemRepository.getTopSellingProducts();
        return raw.stream()
                .limit(limit)
                .map(row -> new TopProductDTO(
                        ((Number) row[0]).longValue(),       // productId
                        (String) row[1],                     // productName
                        (String) row[2],                     // category
                        ((Number) row[3]).longValue(),       // totalQuantitySold
                        toBigDecimal(row[4]),                // totalRevenue
                        (String) row[5]                      // imageUrl
                ))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // 5. Phân tích doanh thu theo danh mục sản phẩm
    // ---------------------------------------------------------------
    public List<CategoryStatsDTO> getCategoryAnalysis() {
        List<Object[]> raw = orderItemRepository.getCategoryStats();
        return raw.stream()
                .map(row -> new CategoryStatsDTO(
                        (String) row[0],                     // category
                        ((Number) row[1]).longValue(),       // totalQuantitySold
                        toBigDecimal(row[2]),                // totalRevenue
                        ((Number) row[3]).longValue()        // distinctProducts
                ))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // 6. Cảnh báo sản phẩm tồn kho thấp
    // ---------------------------------------------------------------
    public List<LowStockProductDTO> getLowStockAlert(int threshold) {
        List<Product> products = productRepository.findLowStockProducts(threshold);
        return products.stream()
                .map(p -> new LowStockProductDTO(
                        p.getId(),
                        p.getProductName(),
                        p.getCategory(),
                        p.getStockQuantity(),
                        threshold,
                        p.getImageUrl()
                ))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // 7. Top khách hàng VIP (mua nhiều nhất)
    // ---------------------------------------------------------------
    public List<TopCustomerDTO> getTopCustomers(int limit) {
        List<Object[]> raw = orderRepository.getTopCustomers();
        return raw.stream()
                .limit(limit)
                .map(row -> new TopCustomerDTO(
                        ((Number) row[0]).longValue(),       // userId
                        (String) row[1],                     // fullName
                        (String) row[2],                     // email
                        ((Number) row[3]).longValue(),       // totalOrders
                        toBigDecimal(row[4])                 // totalSpent
                ))
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // 8. Phân bổ đơn hàng theo trạng thái
    // ---------------------------------------------------------------
    public List<OrderStatusStatsDTO> getOrderStatusBreakdown() {
        List<Object[]> raw = orderRepository.countOrdersByStatus();

        // Tính tổng để tính phần trăm
        long grandTotal = raw.stream()
                .mapToLong(row -> ((Number) row[1]).longValue())
                .sum();

        return raw.stream()
                .map(row -> {
                    String status = (String) row[0];
                    long count = ((Number) row[1]).longValue();
                    double percentage = grandTotal > 0
                            ? Math.round((count * 10000.0 / grandTotal)) / 100.0
                            : 0.0;
                    return new OrderStatusStatsDTO(status, count, percentage);
                })
                .collect(Collectors.toList());
    }

    // ---------------------------------------------------------------
    // Helper: Chuyển Object[] -> DailyRevenueDTO
    // ---------------------------------------------------------------
    private List<DailyRevenueDTO> mapToDailyRevenue(List<Object[]> raw) {
        List<DailyRevenueDTO> result = new ArrayList<>();
        for (Object[] row : raw) {
            String period = (String) row[0];
            BigDecimal revenue = toBigDecimal(row[1]);
            long count = ((Number) row[2]).longValue();
            result.add(new DailyRevenueDTO(period, revenue, count));
        }
        return result;
    }

    private BigDecimal toBigDecimal(Object obj) {
        if (obj == null) return BigDecimal.ZERO;
        if (obj instanceof BigDecimal) return (BigDecimal) obj;
        return BigDecimal.valueOf(((Number) obj).doubleValue());
    }

    // ---------------------------------------------------------------
    // 9. Ghi nhận sự kiện phễu
    // ---------------------------------------------------------------
    public void trackFunnelEvent(String eventType, String sessionId, Long productId) {
        FunnelEvent event = new FunnelEvent();
        event.setEventType(eventType);
        event.setSessionId(sessionId);
        event.setProductId(productId);
        event.setTimestamp(LocalDateTime.now());
        funnelEventRepository.save(event);
    }

    // ---------------------------------------------------------------
    // 10. Lấy thống kê phễu chuyển đổi
    // ---------------------------------------------------------------
    public FunnelStatsDTO getFunnelStats() {
        if (funnelEventRepository.count() == 0) {
            seedFunnelEvents();
        }

        long views = funnelEventRepository.countUniqueSessionsByEventType("VIEW_PRODUCT");
        long carts = funnelEventRepository.countUniqueSessionsByEventType("ADD_TO_CART");
        long checkouts = funnelEventRepository.countUniqueSessionsByEventType("INITIATE_CHECKOUT");
        long purchases = funnelEventRepository.countUniqueSessionsByEventType("PURCHASE");

        // Tính tỉ lệ phần trăm làm tròn đến 1 chữ số thập phân
        double viewToCart = views > 0 ? Math.round((carts * 1000.0) / views) / 10.0 : 0.0;
        double cartToCheckout = carts > 0 ? Math.round((checkouts * 1000.0) / carts) / 10.0 : 0.0;
        double checkoutToPurchase = checkouts > 0 ? Math.round((purchases * 1000.0) / checkouts) / 10.0 : 0.0;
        double overallConversion = views > 0 ? Math.round((purchases * 1000.0) / views) / 10.0 : 0.0;

        return new FunnelStatsDTO(views, carts, checkouts, purchases, viewToCart, cartToCheckout, checkoutToPurchase, overallConversion);
    }

    // ---------------------------------------------------------------
    // Helper: Sinh dữ liệu phễu chuyển đổi mẫu
    // ---------------------------------------------------------------
    private void seedFunnelEvents() {
        java.util.Random random = new java.util.Random();
        int totalSessions = 250; // Tổng số lượt truy cập mẫu lớn hơn một chút để trực quan

        for (int i = 0; i < totalSessions; i++) {
            String sessionId = "session_mock_" + java.util.UUID.randomUUID().toString().substring(0, 8);
            LocalDateTime baseTime = LocalDateTime.now().minusDays(random.nextInt(30)).minusHours(random.nextInt(24));

            // Bước 1: Xem sản phẩm (100% session)
            FunnelEvent viewEvent = new FunnelEvent(null, "VIEW_PRODUCT", sessionId, null, baseTime);
            funnelEventRepository.save(viewEvent);

            // Bước 2: Thêm vào giỏ hàng (~48% tỉ lệ chuyển đổi)
            if (random.nextDouble() < 0.48) {
                FunnelEvent cartEvent = new FunnelEvent(null, "ADD_TO_CART", sessionId, null, baseTime.plusMinutes(2 + random.nextInt(5)));
                funnelEventRepository.save(cartEvent);

                // Bước 3: Tiến hành thanh toán (~52% từ giỏ hàng sang checkout)
                if (random.nextDouble() < 0.52) {
                    FunnelEvent checkoutEvent = new FunnelEvent(null, "INITIATE_CHECKOUT", sessionId, null, baseTime.plusMinutes(7 + random.nextInt(10)));
                    funnelEventRepository.save(checkoutEvent);

                    // Bước 4: Mua hàng thành công (~60% từ checkout sang đơn hàng thành công)
                    if (random.nextDouble() < 0.60) {
                        FunnelEvent purchaseEvent = new FunnelEvent(null, "PURCHASE", sessionId, null, baseTime.plusMinutes(15 + random.nextInt(10)));
                        funnelEventRepository.save(purchaseEvent);
                    }
                }
            }
        }
    }
}
