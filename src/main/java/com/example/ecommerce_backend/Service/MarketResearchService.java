package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Repository.OrderItemRepository;
import com.example.ecommerce_backend.Repository.OrderRepository;
import com.example.ecommerce_backend.Repository.ProductRepository;
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
}
