package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Service.ProductService;
import com.example.ecommerce_backend.dto.ApiResponse;
import com.example.ecommerce_backend.dto.ProductRequestDTO;
import com.example.ecommerce_backend.dto.ProductResponseDTO;
import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.util.UserResolverHelper;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.text.Normalizer;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/manager/products")
public class ManageProductController {

    @Value("${app.upload-dir:uploads}")
    private String uploadDir;

    private final ProductService productService;
    private final UserResolverHelper userResolverHelper;

    public ManageProductController(ProductService productService, UserResolverHelper userResolverHelper) {
        this.productService = productService;
        this.userResolverHelper = userResolverHelper;
    }

    @GetMapping
    public List<ProductResponseDTO> getAllProductsForManager() {
        return productService.getAllProductsForManager();
    }

    @PostMapping
    public ProductResponseDTO createProduct(HttpServletRequest servletRequest, @RequestBody ProductRequestDTO request) {
        User currentUser = userResolverHelper.resolveCurrentUser(servletRequest);
        Role role = currentUser != null ? currentUser.getRole() : Role.STAFF;
        return productService.createProduct(request, role);
    }

    @PutMapping("/{id:\\d+}")
    public ProductResponseDTO updateProduct(HttpServletRequest servletRequest, @PathVariable Long id, @RequestBody ProductRequestDTO request) {
        User currentUser = userResolverHelper.resolveCurrentUser(servletRequest);
        Role role = currentUser != null ? currentUser.getRole() : Role.STAFF;
        return productService.updateProduct(id, request, role);
    }

    @PutMapping("/{id:\\d+}/approve")
    public ProductResponseDTO approveProduct(@PathVariable Long id) {
        return productService.approveProduct(id);
    }

    @PutMapping("/{id:\\d+}/reject")
    public ProductResponseDTO rejectProduct(@PathVariable Long id) {
        return productService.rejectProduct(id);
    }

    @DeleteMapping("/{id:\\d+}")
    public ProductResponseDTO hideProduct(@PathVariable Long id) {
        return productService.hideProduct(id);
    }

    @PostMapping(value = "/upload-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ApiResponse<Map<String, String>> uploadProductImage(@RequestPart("file") MultipartFile file) throws IOException {
        if (file.isEmpty()) {
            throw new RuntimeException("File ảnh đang trống");
        }

        String contentType = file.getContentType();
        if (contentType == null || !contentType.toLowerCase().startsWith("image/")) {
            throw new RuntimeException("Chỉ chấp nhận file hình ảnh");
        }

        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        Files.createDirectories(uploadPath);

        String sanitizedFileName = sanitizeFileName(file.getOriginalFilename());
        String storedFileName = UUID.randomUUID() + "-" + sanitizedFileName;
        Path destinationFile = uploadPath.resolve(storedFileName).normalize();

        Files.copy(file.getInputStream(), destinationFile, StandardCopyOption.REPLACE_EXISTING);

        String imageUrl = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/uploads/")
                .path(storedFileName)
                .toUriString();

        return ApiResponse.ok(Map.of("imageUrl", imageUrl));
    }

    private String sanitizeFileName(String fileName) {
        String normalized = Normalizer.normalize(fileName == null ? "image" : fileName, Normalizer.Form.NFD)
                .replaceAll("\\p{M}", "");
        String sanitized = normalized.replaceAll("[^a-zA-Z0-9._-]", "-").replaceAll("-{2,}", "-");
        return sanitized.isBlank() ? "image" : sanitized;
    }
}
