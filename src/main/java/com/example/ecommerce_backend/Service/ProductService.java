package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductStatus;
import com.example.ecommerce_backend.Repository.ProductRepository;
import com.example.ecommerce_backend.dto.ProductRequestDTO;
import com.example.ecommerce_backend.dto.ProductResponseDTO;
import org.springframework.stereotype.Service;

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
}