package com.example.ecommerce_backend.dto;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

public class ProductRequestDTO {

    private String productName;
    private String description;
    private BigDecimal price;
    private Integer stockQuantity;
    private String imageUrl;
    private String category;
    private String size;
    private String color;
    private List<String> sizes;
    private List<String> colors;

    public ProductRequestDTO() {
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

    public String getCategory() {
        return category;
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

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setSizes(List<String> sizes) {
        this.sizes = sizes;
    }

    public void setColors(List<String> colors) {
        this.colors = colors;
    }

    public List<String> resolveSizes() {
        return mergeValues(sizes, size);
    }

    public List<String> resolveColors() {
        return mergeValues(colors, color);
    }

    private List<String> mergeValues(List<String> multiValues, String singleValue) {
        Set<String> uniqueValues = new LinkedHashSet<>();

        if (multiValues != null) {
            multiValues.stream()
                    .filter(value -> value != null && !value.trim().isEmpty())
                    .map(String::trim)
                    .forEach(uniqueValues::add);
        }

        if (singleValue != null && !singleValue.trim().isEmpty()) {
            Arrays.stream(singleValue.split(","))
                    .map(String::trim)
                    .filter(value -> !value.isEmpty())
                    .forEach(uniqueValues::add);
        }

        return new ArrayList<>(uniqueValues);
    }
}
