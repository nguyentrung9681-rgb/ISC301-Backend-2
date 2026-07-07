package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.CategoryService;
import com.example.ecommerce_backend.dto.CategoryRequestDTO;
import com.example.ecommerce_backend.dto.CategoryResponseDTO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/manager/categories")
public class ManageCategoryController {

    private final CategoryService categoryService;

    public ManageCategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping
    public List<CategoryResponseDTO> getAllCategories() {
        return categoryService.getActiveCategories();
    }

    @PostMapping
    public CategoryResponseDTO createCategory(@RequestBody CategoryRequestDTO request) {
        return categoryService.createCategory(request);
    }

    @PutMapping("/{id:\\d+}")
    public CategoryResponseDTO updateCategory(@PathVariable Long id, @RequestBody CategoryRequestDTO request) {
        return categoryService.updateCategory(id, request);
    }

    @DeleteMapping("/{id:\\d+}")
    public CategoryResponseDTO deleteCategory(@PathVariable Long id) {
        return categoryService.deleteCategory(id);
    }
}
