package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.ProductService;
import com.example.ecommerce_backend.dto.ProductRequestDTO;
import com.example.ecommerce_backend.dto.ProductResponseDTO;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/manager/products")
public class ManageProductController {

    private final ProductService productService;

    public ManageProductController(ProductService productService) {
        this.productService = productService;
    }

    // Manager xem toàn bộ sản phẩm
    // GET: http://localhost:8080/api/manager/products
    @GetMapping
    public List<ProductResponseDTO> getAllProductsForManager() {
        return productService.getAllProductsForManager();
    }

    // Manager thêm sản phẩm mới
    // POST: http://localhost:8080/api/manager/products
    @PostMapping
    public ProductResponseDTO createProduct(@RequestBody ProductRequestDTO request) {
        return productService.createProduct(request);
    }

    // Manager cập nhật sản phẩm
    // PUT: http://localhost:8080/api/manager/products/1
    @PutMapping("/{id}")
    public ProductResponseDTO updateProduct(
            @PathVariable Long id,
            @RequestBody ProductRequestDTO request
    ) {
        return productService.updateProduct(id, request);
    }

    // Manager ẩn sản phẩm khỏi Storefront
    // DELETE: http://localhost:8080/api/manager/products/1
    @DeleteMapping("/{id}")
    public ProductResponseDTO hideProduct(@PathVariable Long id) {
        return productService.hideProduct(id);
    }
}