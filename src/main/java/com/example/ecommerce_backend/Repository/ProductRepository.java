package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByProductStatus(ProductStatus productStatus);

    /** Sản phẩm có tồn kho thấp hơn ngưỡng threshold */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.productStatus = 'ACTIVE' ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}