package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.MarketResearchService;
import com.example.ecommerce_backend.dto.MarketResearchDTO.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Controller cung cấp các API nghiên cứu thị trường cho Admin/Manager.
 * Base URL: /api/admin/market-research
 */
@RestController
@RequestMapping("/api/admin/market-research")
public class MarketResearchController {

    @Autowired
    private MarketResearchService marketResearchService;

    // ---------------------------------------------------------------
    // DOANH THU
    // ---------------------------------------------------------------

    /**
     * Tổng hợp doanh thu theo khoảng thời gian.
     * Bao gồm: tổng doanh thu, số đơn, doanh thu TB/đơn, chi tiết từng ngày.
     *
     * GET /api/admin/market-research/revenue?startDate=2025-01-01&endDate=2025-12-31
     */
    @GetMapping("/revenue")
    public ResponseEntity<RevenueStatsDTO> getRevenueStats(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().withDayOfYear(1).toString()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().toString()}")
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {

        // Mặc định: từ đầu năm đến hôm nay nếu không truyền param
        if (startDate == null) startDate = LocalDate.now().withDayOfYear(1);
        if (endDate == null) endDate = LocalDate.now();

        return ResponseEntity.ok(marketResearchService.getRevenueStats(startDate, endDate));
    }

    /**
     * Doanh thu theo từng tháng trong năm.
     *
     * GET /api/admin/market-research/revenue/monthly?year=2025
     */
    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<DailyRevenueDTO>> getMonthlyRevenue(
            @RequestParam(defaultValue = "#{T(java.time.LocalDate).now().year}") int year) {
        return ResponseEntity.ok(marketResearchService.getMonthlyRevenue(year));
    }

    /**
     * Doanh thu theo từng ngày trong khoảng thời gian.
     *
     * GET /api/admin/market-research/revenue/daily?startDate=2025-06-01&endDate=2025-06-30
     */
    @GetMapping("/revenue/daily")
    public ResponseEntity<List<DailyRevenueDTO>> getDailyRevenue(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate) {
        return ResponseEntity.ok(marketResearchService.getDailyRevenue(startDate, endDate));
    }

    // ---------------------------------------------------------------
    // SẢN PHẨM
    // ---------------------------------------------------------------

    /**
     * Top sản phẩm bán chạy nhất (theo số lượng bán).
     *
     * GET /api/admin/market-research/products/top-selling?limit=10
     */
    @GetMapping("/products/top-selling")
    public ResponseEntity<List<TopProductDTO>> getTopSellingProducts(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(marketResearchService.getTopSellingProducts(limit));
    }

    /**
     * Danh sách sản phẩm có tồn kho thấp (cần nhập thêm hàng).
     *
     * GET /api/admin/market-research/products/low-stock?threshold=10
     */
    @GetMapping("/products/low-stock")
    public ResponseEntity<List<LowStockProductDTO>> getLowStockAlert(
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(marketResearchService.getLowStockAlert(threshold));
    }

    // ---------------------------------------------------------------
    // DANH MỤC SẢN PHẨM
    // ---------------------------------------------------------------

    /**
     * Phân tích doanh thu và sản phẩm bán theo từng danh mục.
     *
     * GET /api/admin/market-research/categories
     */
    @GetMapping("/categories")
    public ResponseEntity<List<CategoryStatsDTO>> getCategoryAnalysis() {
        return ResponseEntity.ok(marketResearchService.getCategoryAnalysis());
    }

    // ---------------------------------------------------------------
    // KHÁCH HÀNG
    // ---------------------------------------------------------------

    /**
     * Top khách hàng VIP chi tiêu nhiều nhất.
     *
     * GET /api/admin/market-research/customers/top?limit=10
     */
    @GetMapping("/customers/top")
    public ResponseEntity<List<TopCustomerDTO>> getTopCustomers(
            @RequestParam(defaultValue = "10") int limit) {
        return ResponseEntity.ok(marketResearchService.getTopCustomers(limit));
    }

    // ---------------------------------------------------------------
    // ĐƠN HÀNG
    // ---------------------------------------------------------------

    /**
     * Phân bổ đơn hàng theo trạng thái (PENDING / SHIPPING / DELIVERED / CANCELLED).
     * Bao gồm số lượng và tỉ lệ phần trăm.
     *
     * GET /api/admin/market-research/orders/status-breakdown
     */
    @GetMapping("/orders/status-breakdown")
    public ResponseEntity<List<OrderStatusStatsDTO>> getOrderStatusBreakdown() {
        return ResponseEntity.ok(marketResearchService.getOrderStatusBreakdown());
    }

    /**
     * Thống kê phễu chuyển đổi (Conversion Funnel).
     *
     * GET /api/admin/market-research/funnel
     */
    @GetMapping("/funnel")
    public ResponseEntity<FunnelStatsDTO> getFunnelStats() {
        return ResponseEntity.ok(marketResearchService.getFunnelStats());
    }
}

