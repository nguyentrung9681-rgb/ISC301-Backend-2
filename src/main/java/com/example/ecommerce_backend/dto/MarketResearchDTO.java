package com.example.ecommerce_backend.dto;

import java.math.BigDecimal;
import java.util.List;

/**
 * Nhóm các DTO dùng cho chức năng Nghiên cứu Thị trường (Market Research).
 */
public class MarketResearchDTO {

    // ---------------------------------------------------------------
    // 1. Thống kê doanh thu tổng hợp
    // ---------------------------------------------------------------
    public static class RevenueStatsDTO {
        private BigDecimal totalRevenue;
        private long totalOrders;
        private BigDecimal averageOrderValue;
        private String startDate;
        private String endDate;
        private List<DailyRevenueDTO> breakdown; // chi tiết theo ngày / tháng

        public RevenueStatsDTO() {}

        public RevenueStatsDTO(BigDecimal totalRevenue, long totalOrders,
                               BigDecimal averageOrderValue,
                               String startDate, String endDate,
                               List<DailyRevenueDTO> breakdown) {
            this.totalRevenue = totalRevenue;
            this.totalOrders = totalOrders;
            this.averageOrderValue = averageOrderValue;
            this.startDate = startDate;
            this.endDate = endDate;
            this.breakdown = breakdown;
        }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

        public BigDecimal getAverageOrderValue() { return averageOrderValue; }
        public void setAverageOrderValue(BigDecimal averageOrderValue) { this.averageOrderValue = averageOrderValue; }

        public String getStartDate() { return startDate; }
        public void setStartDate(String startDate) { this.startDate = startDate; }

        public String getEndDate() { return endDate; }
        public void setEndDate(String endDate) { this.endDate = endDate; }

        public List<DailyRevenueDTO> getBreakdown() { return breakdown; }
        public void setBreakdown(List<DailyRevenueDTO> breakdown) { this.breakdown = breakdown; }
    }

    // ---------------------------------------------------------------
    // 2. Doanh thu theo từng ngày / tháng
    // ---------------------------------------------------------------
    public static class DailyRevenueDTO {
        private String period;        // "2025-06-15" hoặc "2025-06"
        private BigDecimal revenue;
        private long orderCount;

        public DailyRevenueDTO() {}

        public DailyRevenueDTO(String period, BigDecimal revenue, long orderCount) {
            this.period = period;
            this.revenue = revenue;
            this.orderCount = orderCount;
        }

        public String getPeriod() { return period; }
        public void setPeriod(String period) { this.period = period; }

        public BigDecimal getRevenue() { return revenue; }
        public void setRevenue(BigDecimal revenue) { this.revenue = revenue; }

        public long getOrderCount() { return orderCount; }
        public void setOrderCount(long orderCount) { this.orderCount = orderCount; }
    }

    // ---------------------------------------------------------------
    // 3. Sản phẩm bán chạy nhất
    // ---------------------------------------------------------------
    public static class TopProductDTO {
        private Long productId;
        private String productName;
        private String category;
        private long totalQuantitySold;
        private BigDecimal totalRevenue;
        private String imageUrl;

        public TopProductDTO() {}

        public TopProductDTO(Long productId, String productName, String category,
                             long totalQuantitySold, BigDecimal totalRevenue, String imageUrl) {
            this.productId = productId;
            this.productName = productName;
            this.category = category;
            this.totalQuantitySold = totalQuantitySold;
            this.totalRevenue = totalRevenue;
            this.imageUrl = imageUrl;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public long getTotalQuantitySold() { return totalQuantitySold; }
        public void setTotalQuantitySold(long totalQuantitySold) { this.totalQuantitySold = totalQuantitySold; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    // ---------------------------------------------------------------
    // 4. Thống kê theo danh mục sản phẩm
    // ---------------------------------------------------------------
    public static class CategoryStatsDTO {
        private String category;
        private long totalQuantitySold;
        private BigDecimal totalRevenue;
        private long distinctProducts;

        public CategoryStatsDTO() {}

        public CategoryStatsDTO(String category, long totalQuantitySold,
                                BigDecimal totalRevenue, long distinctProducts) {
            this.category = category;
            this.totalQuantitySold = totalQuantitySold;
            this.totalRevenue = totalRevenue;
            this.distinctProducts = distinctProducts;
        }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public long getTotalQuantitySold() { return totalQuantitySold; }
        public void setTotalQuantitySold(long totalQuantitySold) { this.totalQuantitySold = totalQuantitySold; }

        public BigDecimal getTotalRevenue() { return totalRevenue; }
        public void setTotalRevenue(BigDecimal totalRevenue) { this.totalRevenue = totalRevenue; }

        public long getDistinctProducts() { return distinctProducts; }
        public void setDistinctProducts(long distinctProducts) { this.distinctProducts = distinctProducts; }
    }

    // ---------------------------------------------------------------
    // 5. Cảnh báo sản phẩm tồn kho thấp
    // ---------------------------------------------------------------
    public static class LowStockProductDTO {
        private Long productId;
        private String productName;
        private String category;
        private int stockQuantity;
        private int threshold;
        private String imageUrl;

        public LowStockProductDTO() {}

        public LowStockProductDTO(Long productId, String productName, String category,
                                  int stockQuantity, int threshold, String imageUrl) {
            this.productId = productId;
            this.productName = productName;
            this.category = category;
            this.stockQuantity = stockQuantity;
            this.threshold = threshold;
            this.imageUrl = imageUrl;
        }

        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }

        public String getProductName() { return productName; }
        public void setProductName(String productName) { this.productName = productName; }

        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }

        public int getStockQuantity() { return stockQuantity; }
        public void setStockQuantity(int stockQuantity) { this.stockQuantity = stockQuantity; }

        public int getThreshold() { return threshold; }
        public void setThreshold(int threshold) { this.threshold = threshold; }

        public String getImageUrl() { return imageUrl; }
        public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    }

    // ---------------------------------------------------------------
    // 6. Khách hàng mua nhiều nhất (VIP)
    // ---------------------------------------------------------------
    public static class TopCustomerDTO {
        private Long userId;
        private String fullName;
        private String email;
        private long totalOrders;
        private BigDecimal totalSpent;

        public TopCustomerDTO() {}

        public TopCustomerDTO(Long userId, String fullName, String email,
                              long totalOrders, BigDecimal totalSpent) {
            this.userId = userId;
            this.fullName = fullName;
            this.email = email;
            this.totalOrders = totalOrders;
            this.totalSpent = totalSpent;
        }

        public Long getUserId() { return userId; }
        public void setUserId(Long userId) { this.userId = userId; }

        public String getFullName() { return fullName; }
        public void setFullName(String fullName) { this.fullName = fullName; }

        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }

        public long getTotalOrders() { return totalOrders; }
        public void setTotalOrders(long totalOrders) { this.totalOrders = totalOrders; }

        public BigDecimal getTotalSpent() { return totalSpent; }
        public void setTotalSpent(BigDecimal totalSpent) { this.totalSpent = totalSpent; }
    }

    // ---------------------------------------------------------------
    // 7. Phân bổ đơn hàng theo trạng thái
    // ---------------------------------------------------------------
    public static class OrderStatusStatsDTO {
        private String status;
        private long count;
        private double percentage;

        public OrderStatusStatsDTO() {}

        public OrderStatusStatsDTO(String status, long count, double percentage) {
            this.status = status;
            this.count = count;
            this.percentage = percentage;
        }

        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }

        public long getCount() { return count; }
        public void setCount(long count) { this.count = count; }

        public double getPercentage() { return percentage; }
        public void setPercentage(double percentage) { this.percentage = percentage; }
    }
}
