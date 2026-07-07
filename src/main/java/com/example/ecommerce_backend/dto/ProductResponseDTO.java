package com.example.ecommerce_backend.dto;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductStatus;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

public class ProductResponseDTO {

    private Long id;
    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private BigDecimal ratingAverage;
    private String category;
    private String categoryLabel;
    private String size;
    private String color;
    private List<String> sizes;
    private List<String> colors;
    private ProductStatus productStatus;
    private Long soldCount = 0L;


    public ProductResponseDTO(Product product, String categoryLabel) {
        this.id = product.getId();
        this.productName = product.getProductName();
        this.description = product.getDescription();
        this.price = product.getPrice();
        this.stockQuantity = product.getStockQuantity();
        this.imageUrl = product.getImageUrl();
        this.ratingAverage = product.getRatingAverage();
        this.category = product.getCategory();
        this.categoryLabel = categoryLabel;
        this.size = product.getSize();
        this.color = product.getColor();
        this.sizes = parseCsv(product.getSize());
        this.colors = parseCsv(product.getColor());
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

    public String getCategoryLabel() {
        return categoryLabel;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public List<String> getSizes() {
        return sizes;
    }

    public List<String> getColors() {
        return colors;
    }

    public ProductStatus getProductStatus() {
        return productStatus;
    }

    public Long getSoldCount() {
        return soldCount;
    }

    public void setSoldCount(Long soldCount) {
        this.soldCount = soldCount;
    }


    private List<String> parseCsv(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return List.of();
        }

        return Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .toList();
    }
}
