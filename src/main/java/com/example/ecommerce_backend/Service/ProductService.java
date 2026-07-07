package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.ProductStatus;
import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Repository.ProductRepository;
import com.example.ecommerce_backend.Repository.OrderItemRepository;
import com.example.ecommerce_backend.dto.ProductRequestDTO;
import com.example.ecommerce_backend.dto.ProductResponseDTO;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryService categoryService;
    private final OrderItemRepository orderItemRepository;

    public ProductService(ProductRepository productRepository, CategoryService categoryService, OrderItemRepository orderItemRepository) {
        this.productRepository = productRepository;
        this.categoryService = categoryService;
        this.orderItemRepository = orderItemRepository;
    }

    private Map<Long, Long> getSoldCountsMap(List<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return Map.of();
        }
        List<Object[]> raw = orderItemRepository.getSoldCountsForProducts(productIds);
        Map<Long, Long> map = new java.util.HashMap<>();
        for (Object[] row : raw) {
            if (row != null && row.length >= 2 && row[0] != null) {
                map.put(((Number) row[0]).longValue(), row[1] != null ? ((Number) row[1]).longValue() : 0L);
            }
        }
        return map;
    }


    public List<ProductResponseDTO> getAllProductsForManager() {
        List<Product> products = productRepository.findAll();
        List<Long> ids = products.stream().map(Product::getId).toList();
        Map<Long, Long> soldCounts = getSoldCountsMap(ids);
        return products.stream()
                .map(p -> {
                    ProductResponseDTO dto = toResponse(p);
                    dto.setSoldCount(soldCounts.getOrDefault(p.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    public List<ProductResponseDTO> getActiveProducts() {
        List<Product> products = productRepository.findByProductStatus(ProductStatus.ACTIVE);
        List<Long> ids = products.stream().map(Product::getId).toList();
        Map<Long, Long> soldCounts = getSoldCountsMap(ids);
        return products.stream()
                .map(p -> {
                    ProductResponseDTO dto = toResponse(p);
                    dto.setSoldCount(soldCounts.getOrDefault(p.getId(), 0L));
                    return dto;
                })
                .toList();
    }

    public ProductResponseDTO createProduct(ProductRequestDTO request, Role creatorRole) {
        validateProductRequest(request);

        Product product = new Product();
        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(categoryService.normalizeCategoryCode(request.getCategory()));
        product.setSize(toCsv(request.resolveSizes()));
        product.setColor(toCsv(request.resolveColors()));
        
        if (creatorRole == Role.MANAGER) {
            product.setProductStatus(ProductStatus.ACTIVE);
        } else {
            product.setProductStatus(ProductStatus.PENDING);
        }

        Product savedProduct = productRepository.save(product);
        return toResponse(savedProduct);
    }

    public ProductResponseDTO updateProduct(Long id, ProductRequestDTO request, Role editorRole) {
        validateProductRequest(request);

        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setProductName(request.getProductName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStockQuantity(request.getStockQuantity());
        product.setImageUrl(request.getImageUrl());
        product.setCategory(categoryService.normalizeCategoryCode(request.getCategory()));
        product.setSize(toCsv(request.resolveSizes()));
        product.setColor(toCsv(request.resolveColors()));

        if (editorRole == Role.STAFF) {
            product.setProductStatus(ProductStatus.PENDING); // Staff edits go back to pending review
        } else {
            product.setProductStatus(ProductStatus.ACTIVE); // Manager edits go live immediately
        }

        Product updatedProduct = productRepository.save(product);
        ProductResponseDTO dto = toResponse(updatedProduct);
        dto.setSoldCount(orderItemRepository.getSoldCountForProduct(id));
        return dto;
    }

    public ProductResponseDTO approveProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setProductStatus(ProductStatus.ACTIVE);
        Product savedProduct = productRepository.save(product);
        ProductResponseDTO dto = toResponse(savedProduct);
        dto.setSoldCount(orderItemRepository.getSoldCountForProduct(id));
        return dto;
    }

    public ProductResponseDTO rejectProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));
        product.setProductStatus(ProductStatus.REJECTED);
        Product savedProduct = productRepository.save(product);
        ProductResponseDTO dto = toResponse(savedProduct);
        dto.setSoldCount(orderItemRepository.getSoldCountForProduct(id));
        return dto;
    }

    public ProductResponseDTO hideProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        product.setProductStatus(ProductStatus.HIDDEN);
        Product hiddenProduct = productRepository.save(product);
        ProductResponseDTO dto = toResponse(hiddenProduct);
        dto.setSoldCount(orderItemRepository.getSoldCountForProduct(id));
        return dto;
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

    public Page<ProductResponseDTO> getStorefrontProducts(
            String keyword,
            String category,
            String size,
            String color,
            BigDecimal minPrice,
            BigDecimal maxPrice,
            Pageable pageable
    ) {
        Specification<Product> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            predicates.add(cb.equal(root.get("productStatus"), ProductStatus.ACTIVE));

            if (keyword != null && !keyword.trim().isEmpty()) {
                String searchPattern = "%" + keyword.trim().toLowerCase(Locale.ROOT) + "%";
                Predicate namePredicate = cb.like(cb.lower(root.get("productName")), searchPattern);
                Predicate descPredicate = cb.like(cb.lower(root.get("description")), searchPattern);
                predicates.add(cb.or(namePredicate, descPredicate));
            }

            if (category != null && !category.trim().isEmpty()) {
                predicates.add(cb.equal(root.get("category"), category.trim()));
            }

            List<String> normalizedSizes = normalizeFilterValues(size);
            if (!normalizedSizes.isEmpty()) {
                predicates.add(buildCsvContainsPredicate(root.get("size"), normalizedSizes, cb));
            }

            List<String> normalizedColors = normalizeFilterValues(color);
            if (!normalizedColors.isEmpty()) {
                predicates.add(buildCsvContainsPredicate(root.get("color"), normalizedColors, cb));
            }

            if (minPrice != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("price"), minPrice));
            }

            if (maxPrice != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("price"), maxPrice));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        Page<Product> page = productRepository.findAll(spec, pageable);
        List<Long> ids = page.stream().map(Product::getId).toList();
        Map<Long, Long> soldCounts = getSoldCountsMap(ids);
        return page.map(p -> {
            ProductResponseDTO dto = toResponse(p);
            dto.setSoldCount(soldCounts.getOrDefault(p.getId(), 0L));
            return dto;
        });
    }

    private List<String> normalizeFilterValues(String rawValue) {
        if (rawValue == null || rawValue.trim().isEmpty()) {
            return List.of();
        }

        return java.util.Arrays.stream(rawValue.split(","))
                .map(String::trim)
                .filter(value -> !value.isEmpty())
                .map(value -> value.toLowerCase(Locale.ROOT))
                .distinct()
                .toList();
    }

    private Predicate buildCsvContainsPredicate(jakarta.persistence.criteria.Path<String> field, List<String> values,
                                                jakarta.persistence.criteria.CriteriaBuilder cb) {
        jakarta.persistence.criteria.Expression<String> csvExpression =
                cb.lower(cb.concat(cb.concat(",", cb.coalesce(field, "")), ","));

        List<Predicate> valuePredicates = values.stream()
                .map(value -> cb.like(csvExpression, "%," + value + ",%"))
                .toList();

        return cb.or(valuePredicates.toArray(new Predicate[0]));
    }

    private String toCsv(List<String> values) {
        if (values == null || values.isEmpty()) {
            return null;
        }

        Map<String, String> uniqueValues = new LinkedHashMap<>();
        values.stream()
                .filter(value -> value != null && !value.trim().isEmpty())
                .map(String::trim)
                .forEach(value -> uniqueValues.putIfAbsent(value.toLowerCase(Locale.ROOT), value));

        if (uniqueValues.isEmpty()) {
            return null;
        }

        return String.join(",", uniqueValues.values());
    }

    public ProductResponseDTO getActiveProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Product not found with id: " + id));

        if (product.getProductStatus() != ProductStatus.ACTIVE) {
            throw new RuntimeException("Product is not active or has been hidden");
        }

        ProductResponseDTO dto = toResponse(product);
        dto.setSoldCount(orderItemRepository.getSoldCountForProduct(id));
        return dto;
    }

    private ProductResponseDTO toResponse(Product product) {
        return new ProductResponseDTO(product, categoryService.resolveCategoryLabel(product.getCategory()));
    }
}
