package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * Top sản phẩm bán chạy nhất (chỉ tính từ đơn DELIVERED).
     * Trả về [productId, productName, category, totalQty, totalRevenue, imageUrl]
     */
    @Query("SELECT oi.product.id, oi.product.productName, oi.product.category, " +
           "SUM(oi.quantity), SUM(oi.price * oi.quantity), oi.product.imageUrl " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status = 'DELIVERED' " +
           "GROUP BY oi.product.id, oi.product.productName, oi.product.category, oi.product.imageUrl " +
           "ORDER BY SUM(oi.quantity) DESC")
    List<Object[]> getTopSellingProducts();

    /**
     * Thống kê doanh thu và sản phẩm bán theo danh mục (chỉ đơn DELIVERED).
     * Trả về [category, totalQty, totalRevenue, distinctProducts]
     */
    @Query("SELECT oi.product.category, SUM(oi.quantity), SUM(oi.price * oi.quantity), COUNT(DISTINCT oi.product.id) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status = 'DELIVERED' " +
           "GROUP BY oi.product.category " +
           "ORDER BY SUM(oi.price * oi.quantity) DESC")
    List<Object[]> getCategoryStats();
    @Query("SELECT oi.product.id, SUM(oi.quantity) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status = 'DELIVERED' AND oi.product.id IN :productIds " +
           "GROUP BY oi.product.id")
    List<Object[]> getSoldCountsForProducts(@Param("productIds") List<Long> productIds);

    @Query("SELECT COALESCE(SUM(oi.quantity), 0L) " +
           "FROM OrderItem oi " +
           "WHERE oi.order.status = 'DELIVERED' AND oi.product.id = :productId")
    Long getSoldCountForProduct(@Param("productId") Long productId);
}
