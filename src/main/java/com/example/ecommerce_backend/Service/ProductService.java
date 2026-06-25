package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductStatus;
import com.example.ecommerce_backend.Repository.ProductRepository;
import com.example.ecommerce_backend.dto.ProductRequestDTO;
import com.example.ecommerce_backend.dto.ProductResponseDTO;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;

import java.math.BigDecimal;
import java.util.List;

@Service
public class ProductService {

    private final ProductRepository productRepository;

    public ProductService(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    // Manager xem toàn bộ sản phẩm, bao gồm cả ACTIVE và HIDDEN
    public List<ProductResponseDTO> getAllProductsForManager() {
        return productRepository.findAll()
                .stream()
                .map(ProductResponseDTO::new)
                .toList();
    }

    // Storefront chỉ lấy sản phẩm ACTIVE
    public List<ProductResponseDTO> getActiveProducts() {
        return productRepository.findByProductStatus(ProductStatus.ACTIVE)
                .stream()
                .map(ProductResponseDTO::new)
                .toList();
    }

    // Create product
    public ProductResponseDTO createProduct(ProductRequestDTO request) {
        validateProductRequest(request);

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());
        product.setProductStatus(ProductStatus.ACTIVE);

        Product savedProduct = productRepository.save(product);

        return new ProductResponseDTO(savedProduct);
    }

    // Update product
    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request) {
        validateProductRequest(request);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(request.getCategory());

        Product updatedProduct = productRepository.save(product);

        return new ProductResponseDTO(updatedProduct);
    }

    // Delete product / Ẩn sản phẩm
    public ProductResponseDTO hideProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setProductStatus(ProductStatus.HIDDEN);

        Product hiddenProduct = productRepository.save(product);

        return new ProductResponseDTO(hiddenProduct);
    }

    private void validateProductRequest(ProductRequestDTO request) {
        if (request.getProductName() == null || request.getProductName().trim().isEmpty()) {
            throw new RuntimeException("Product name cannot be empty");
        }

        if (request.getPrice() == null || request.getPrice().compareTo(BigDecimal.ZERO) <= 0) {
            throw new RuntimeException("Price must be greater than 0");
        }

        if (request.getStockQuantity() == null || request.getStockQuantity() < 0) {
            throw new RuntimeException("Stock quantity cannot be negative");
        }

        if (request.getCategory() == null || request.getCategory().trim().isEmpty()) {
            throw new RuntimeException("Category cannot be empty");
        }
    }

    // Lọc, tìm kiếm, sắp xếp và phân trang sản phẩm cho Storefront (chỉ lấy ACTIVE)
    public Page<ProductResponseDTO> getStorefrontProducts(
            String keyword,
            String category,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Chỉ lấy sản phẩm ACTIVE
            predicates.add(cb.equal(root.get("productStatus"), ProductStatus.ACTIVE));

            // Tìm theo từ khóa (tên hoặc mô tả)
            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase() + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("productName")), searchPattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(namePredicate, descPredicate));
            }

            // Lọc theo danh mục
            if (category != null && !category.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category.trim()));
            }

            // Lọc theo khoảng giá
            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }
            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return productRepository.findAll(spec, pageable)
                .map(ProductResponseDTO::new);
    }

    // Chi tiết sản phẩm ACTIVE cho Storefront
    public ProductResponseDTO getActiveProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (product.getProductStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Product is not active or has been hidden");
        }

        return new ProductResponseDTO(product);
    }
}