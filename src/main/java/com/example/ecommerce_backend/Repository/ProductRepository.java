package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product> {

    List<Product> findByProductStatus(ProductStatus productStatus);

    long countByCategoryIgnoreCase(String category);

    @Query("SELECT DISTINCT p.category FROM Product p WHERE p.category IS NOT NULL AND TRIM(p.category) <> ''")
    List<String> findDistinctCategories();

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.category = :newCategory WHERE LOWER(p.category) = LOWER(:oldCategory)")
    int updateCategoryReference(@Param("oldCategory") String oldCategory, @Param("newCategory") String newCategory);

    @Query("SELECT p FROM Product p WHERE p.stockQuantity <= :threshold AND p.productStatus = 'ACTIVE' ORDER BY p.stockQuantity ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}
