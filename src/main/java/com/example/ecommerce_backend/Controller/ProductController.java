package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.ProductService;
import com.example.ecommerce_backend.dto.ProductResponseDTO;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    // Khách hàng xem danh sách sản phẩm với bộ lọc, tìm kiếm, sắp xếp và phân trang
    // GET: http://localhost:8080/api/products
    @GetMapping
    public ResponseEntity<Page<ProductResponseDTO>> getStorefrontProducts(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "id") String sortBy,
            @RequestParam(defaultValue = "asc") String sortDir
    ) {
        Sort sort = sortDir.equalsIgnoreCase(Sort.Direction.ASC.name())
                ? Sort.by(sortBy).ascending()
                : Sort.by(sortBy).descending();

        Pageable pageable = PageRequest.of(page, size, sort);

        Page<ProductResponseDTO> products = productService.getStorefrontProducts(
                keyword, category, minPrice, maxPrice, pageable
        );

        return ResponseEntity.ok(products);
    }

    // Khách hàng xem chi tiết 1 sản phẩm đang hoạt động (ACTIVE)
    // GET: http://localhost:8080/api/products/1
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponseDTO> getActiveProductById(@PathVariable Long id) {
        ProductResponseDTO product = productService.getActiveProductById(id);
        return ResponseEntity.ok(product);
    }
}
