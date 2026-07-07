package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Category;
import com.example.ecommerce_backend.Repository.CategoryRepository;
import com.example.ecommerce_backend.Repository.ProductRepository;
import com.example.ecommerce_backend.dto.CategoryRequestDTO;
import com.example.ecommerce_backend.dto.CategoryResponseDTO;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.text.Normalizer;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    public CategoryService(CategoryRepository categoryRepository, ProductRepository productRepository) {
        this.categoryRepository = categoryRepository;
        this.productRepository = productRepository;
    }

    @PostConstruct
    @Transactional
    public void initializeCategories() {
        ensureCategory("ao", "Áo");
        ensureCategory("quan", "Quần");
        ensureCategory("vay", "Váy");
        ensureCategory("phukien", "Phụ kiện");
    }

    public List<CategoryResponseDTO> getActiveCategories() {
        return categoryRepository.findByActiveTrueOrderByNameAsc()
                .stream()
                .map(CategoryResponseDTO::new)
                .toList();
    }

    public String resolveCategoryLabel(String categoryValue) {
        if (categoryValue == null || categoryValue.trim().isEmpty()) {
            return "Sản phẩm";
        }

        String trimmedValue = categoryValue.trim();

        return findByCodeOrName(trimmedValue)
                .map(Category::getName)
                .orElse(trimmedValue);
    }

    public String normalizeCategoryCode(String categoryValue) {
        if (categoryValue == null || categoryValue.trim().isEmpty()) {
            throw new RuntimeException("Category cannot be empty");
        }

        String trimmedValue = categoryValue.trim();

        return findByCodeOrName(trimmedValue)
                .map(Category::getCode)
                .orElseThrow(() -> new RuntimeException("Category does not exist"));
    }

    @Transactional
    public CategoryResponseDTO createCategory(CategoryRequestDTO request) {
        String name = normalizeName(request.getName());
        String code = normalizeRequestedCode(request.getCode(), name);
        validateUnique(code, name, null);

        Category category = new Category();
        category.setCode(code);
        category.setName(name);
        category.setActive(true);

        return new CategoryResponseDTO(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponseDTO updateCategory(Long id, CategoryRequestDTO request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        String oldCode = category.getCode();
        String oldName = category.getName();
        String name = normalizeName(request.getName());
        String code = normalizeRequestedCode(request.getCode(), name);

        validateUnique(code, name, id);

        category.setCode(code);
        category.setName(name);
        category.setActive(true);
        Category savedCategory = categoryRepository.save(category);

        if (!oldCode.equalsIgnoreCase(code)) {
            productRepository.updateCategoryReference(oldCode, code);
        }
        if (!oldName.equalsIgnoreCase(code)) {
            productRepository.updateCategoryReference(oldName, code);
        }

        return new CategoryResponseDTO(savedCategory);
    }

    @Transactional
    public CategoryResponseDTO deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        long productCount = productRepository.countByCategoryIgnoreCase(category.getCode());
        if (productCount == 0) {
            productCount = productRepository.countByCategoryIgnoreCase(category.getName());
        }

        if (productCount > 0) {
            throw new RuntimeException("Cannot delete category because it is being used by " + productCount + " product(s)");
        }

        category.setActive(false);
        Category deletedCategory = categoryRepository.save(category);
        return new CategoryResponseDTO(deletedCategory);
    }

    private void ensureCategory(String code, String name) {
        Optional<Category> categoryByCode = categoryRepository.findByCodeIgnoreCase(code);
        if (categoryByCode.isPresent()) {
            Category category = categoryByCode.get();
            boolean changed = false;

            if (!name.equals(category.getName())) {
                category.setName(name);
                changed = true;
            }
            if (!category.isActive()) {
                category.setActive(true);
                changed = true;
            }
            if (changed) {
                categoryRepository.save(category);
            }
            return;
        }

        Optional<Category> categoryByName = categoryRepository.findByNameIgnoreCase(name);
        if (categoryByName.isPresent()) {
            Category category = categoryByName.get();
            if (!code.equalsIgnoreCase(category.getCode()) || !category.isActive()) {
                category.setCode(code);
                category.setActive(true);
                categoryRepository.save(category);
            }
            return;
        }

        Category category = new Category();
        category.setCode(code);
        category.setName(name);
        category.setActive(true);
        categoryRepository.save(category);
    }

    private Optional<Category> findByCodeOrName(String value) {
        return categoryRepository.findByCodeIgnoreCase(value)
                .or(() -> categoryRepository.findByNameIgnoreCase(value));
    }

    private void validateUnique(String code, String name, Long currentId) {
        categoryRepository.findByCodeIgnoreCase(code)
                .filter(category -> !category.getId().equals(currentId))
                .ifPresent(category -> {
                    throw new RuntimeException("Category code already exists");
                });

        categoryRepository.findByNameIgnoreCase(name)
                .filter(category -> !category.getId().equals(currentId))
                .ifPresent(category -> {
                    throw new RuntimeException("Category name already exists");
                });
    }

    private String normalizeName(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new RuntimeException("Category name cannot be empty");
        }
        return name.trim();
    }

    private String normalizeRequestedCode(String requestedCode, String fallbackName) {
        String resolved = requestedCode == null || requestedCode.trim().isEmpty()
                ? slugify(fallbackName)
                : slugify(requestedCode);

        if (resolved.isBlank()) {
            throw new RuntimeException("Category code cannot be empty");
        }

        return resolved;
    }

    private String slugify(String rawValue) {
        String normalized = Normalizer.normalize(rawValue == null ? "" : rawValue, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "")
                .replace('đ', 'd')
                .replace('Đ', 'D')
                .toLowerCase(Locale.ROOT)
                .replaceAll("[^a-z0-9]+", "-")
                .replaceAll("(^-|-$)", "");

        return normalized.isBlank() ? "danh-muc" : normalized;
    }
}
