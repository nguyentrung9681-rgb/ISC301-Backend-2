package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.Role;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Service.ReviewService;
import com.example.ecommerce_backend.dto.ReviewRequestDTO;
import com.example.ecommerce_backend.dto.ReviewResponseDTO;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

import static com.example.ecommerce_backend.Entity.AccountStatus.ACTIVE;

@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    // Giả định MockUser tạm thời cho Client test (thay bằng Spring Security sau)
    private User getMockUser() {
        return new User(1L, "khachhang@gmail.com", "", "Nguyen Van A", "0987654321", "Ha Noi", Role.BUYER, ACTIVE);
    }

    // POST /api/client/review/add — Client gửi đánh giá sản phẩm
    @PostMapping("/client/review/add")
    public ResponseEntity<ReviewResponseDTO> addReview(@Valid @RequestBody ReviewRequestDTO dto) {
        User currentUser = getMockUser();
        return ResponseEntity.ok(
                ReviewResponseDTO.fromEntity(reviewService.addReview(dto, currentUser))
        );
    }

    // GET /api/client/review/product/{productId} — Public, xem đánh giá của sản phẩm
    @GetMapping("/client/review/product/{productId}")
    public ResponseEntity<List<ReviewResponseDTO>> getProductReviews(@PathVariable Long productId) {
        List<ReviewResponseDTO> dtos = reviewService.getReviewsByProduct(productId)
                .stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(dtos);
    }

    // DELETE /api/manager/review/{id} — Manager xóa đánh giá vi phạm
    @DeleteMapping("/manager/review/{id}")
    public ResponseEntity<String> deleteReviewByManager(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok("Đã gỡ bỏ bài đánh giá vi phạm (id=" + id + ")");
    }
}
