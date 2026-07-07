package com.example.ecommerce_backend.dto;

import com.example.ecommerce_backend.Entity.Category;

public class CategoryResponseDTO {

    private Long id;
    private String code;
    private String name;

    public CategoryResponseDTO(Category category) {
        this.id = category.getId();
        this.code = category.getCode();
        this.name = category.getName();
    }

    public Long getId() {
        return id;
    }

    public String getCode() {
        return code;
    }

    public String getName() {
        return name;
    }
}
