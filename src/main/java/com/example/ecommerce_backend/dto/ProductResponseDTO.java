package com.example.ecommerce_backend.dto;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductStatus;

import java.math.BigDecimal;

public class ProductResponseDTO {

    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private BigDecimal ratingAverage;
    private String category;
    private ProductStatus productStatus;

    public ProductResponseDTO(Product product) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.imageUrl = product.getImageUrl();
        this.ratingAverage = product.getRatingAverage();
        this.category = product.getCategory();
        this.productStatus = product.getProductStatus();
    }

    public Long getId() {
        return id;
    }

    public String getProductName() {
        return productName;
    }

    public String getDescription() {
        return description;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public BigDecimal getRatingAverage() {
        return ratingAverage;
    }

    public String getCategory() {
        return category;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }
}