package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Query("""
           SELECT DISTINCT o FROM Order o
           LEFT JOIN FETCH o.user
           LEFT JOIN FETCH o.orderItems oi
           LEFT JOIN FETCH oi.product
           ORDER BY o.orderDate DESC
           """)
    List<Order> findAllWithDetails();

    @Query("""
           SELECT o FROM Order o
           LEFT JOIN FETCH o.user
           LEFT JOIN FETCH o.orderItems oi
           LEFT JOIN FETCH oi.product
           WHERE o.id = :id
           """)
    java.util.Optional<Order> findByIdWithDetails(@Param("id") Long id);

    List<Order> findByUserIdOrderByOrderDateDesc(Long userID); //lich su mua hang cua Client

    // ---------------------------------------------------------------
    // Market Research Queries
    // ---------------------------------------------------------------

    /** Tổng doanh thu từ các đơn DELIVERED trong khoảng thời gian */
    @Query("SELECT COALESCE(SUM(o.totalAmount), 0) FROM Order o " +
           "WHERE o.status = 'DELIVERED' AND o.orderDate BETWEEN :start AND :end")
    BigDecimal getTotalRevenue(@Param("start") LocalDateTime start,
                               @Param("end") LocalDateTime end);

    /** Số đơn DELIVERED trong khoảng thời gian */
    @Query("SELECT COUNT(o) FROM Order o " +
           "WHERE o.status = 'DELIVERED' AND o.orderDate BETWEEN :start AND :end")
    long countDeliveredOrders(@Param("start") LocalDateTime start,
                              @Param("end") LocalDateTime end);

    /** Đếm số đơn theo từng trạng thái */
    @Query("SELECT o.status, COUNT(o) FROM Order o GROUP BY o.status")
    List<Object[]> countOrdersByStatus();

    /**
     * Doanh thu theo từng ngày trong khoảng thời gian (đơn DELIVERED).
     * Trả về [date_string, revenue, orderCount]
     */
    @Query("SELECT FUNCTION('to_char', o.orderDate, 'YYYY-MM-DD') AS day, " +
           "COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o " +
           "WHERE o.status = 'DELIVERED' AND o.orderDate BETWEEN :start AND :end " +
           "GROUP BY FUNCTION('to_char', o.orderDate, 'YYYY-MM-DD') " +
           "ORDER BY FUNCTION('to_char', o.orderDate, 'YYYY-MM-DD') ASC")
    List<Object[]> getRevenueByDay(@Param("start") LocalDateTime start,
                                   @Param("end") LocalDateTime end);

    /**
     * Doanh thu theo từng tháng trong năm (đơn DELIVERED).
     * Trả về [month_string e.g "2025-06", revenue, orderCount]
     */
    @Query("SELECT FUNCTION('to_char', o.orderDate, 'YYYY-MM') AS month, " +
           "COALESCE(SUM(o.totalAmount), 0), COUNT(o) " +
           "FROM Order o " +
           "WHERE o.status = 'DELIVERED' " +
           "AND year(o.orderDate) = :year " +
           "GROUP BY FUNCTION('to_char', o.orderDate, 'YYYY-MM') " +
           "ORDER BY FUNCTION('to_char', o.orderDate, 'YYYY-MM') ASC")
    List<Object[]> getRevenueByMonth(@Param("year") int year);

    /**
     * Top khách hàng chi tiêu nhiều nhất (đơn DELIVERED).
     * Trả về [userId, fullName, email, orderCount, totalSpent]
     */
    @Query("SELECT o.user.id, o.user.fullName, o.user.email, COUNT(o), COALESCE(SUM(o.totalAmount), 0) " +
           "FROM Order o " +
           "WHERE o.status = 'DELIVERED' " +
           "GROUP BY o.user.id, o.user.fullName, o.user.email " +
           "ORDER BY COALESCE(SUM(o.totalAmount), 0) DESC")
    List<Object[]> getTopCustomers();
}
