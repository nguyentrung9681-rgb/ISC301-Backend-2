package com.example.ecommerce_backend.Controller;

import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Service.ReviewService;
import com.example.ecommerce_backend.dto.ApiResponse;
import com.example.ecommerce_backend.dto.ReviewRequestDTO;
import com.example.ecommerce_backend.dto.ReviewResponseDTO;
import com.example.ecommerce_backend.util.UserResolverHelper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api")
public class ReviewController {

    @Autowired
    private ReviewService reviewService;

    @Autowired
    private UserResolverHelper userResolverHelper;

    // POST /api/client/review/add — Client gửi đánh giá sản phẩm
    @PostMapping("/client/review/add")
    public ResponseEntity<ApiResponse<?>> addReview(@Valid @RequestBody ReviewRequestDTO dto, HttpServletRequest request) {
        User currentUser = userResolverHelper.resolveCurrentUser(request);
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.error("Yêu cầu đăng nhập để thực hiện đánh giá"));
        }
        ReviewResponseDTO result = ReviewResponseDTO.fromEntity(reviewService.addReview(dto, currentUser));
        return ResponseEntity.ok(ApiResponse.ok("Đánh giá đã được gửi thành công", result));
    }

    // GET /api/client/review/product/{productId} — Public, xem đánh giá của sản phẩm
    @GetMapping("/client/review/product/{productId}")
    public ResponseEntity<ApiResponse<List<ReviewResponseDTO>>> getProductReviews(@PathVariable Long productId) {
        List<ReviewResponseDTO> dtos = reviewService.getReviewsByProduct(productId)
                .stream()
                .map(ReviewResponseDTO::fromEntity)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.ok(dtos));
    }

    // DELETE /api/manager/review/{id} — Manager xóa đánh giá vi phạm
    @DeleteMapping("/manager/review/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteReviewByManager(@PathVariable Long id) {
        reviewService.deleteReview(id);
        return ResponseEntity.ok(ApiResponse.ok("Đã gỡ bỏ bài đánh giá vi phạm (id=" + id + ")", null));
    }
}
