package com.example.ecommerce_backend.Repository;

import com.example.ecommerce_backend.Entity.CartItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItem, Long> {
    List<CartItem> findByUserIdAndIsWishlist(Long userId, boolean isWishlist);
    Optional<CartItem> findByUserIdAndProductIdAndIsWishlist(Long userId, Long productId, boolean isWishlist);
    Optional<CartItem> findByUserIdAndProductIdAndSelectedSizeAndSelectedColorAndIsWishlist(
            Long userId, Long productId, String selectedSize, String selectedColor, boolean isWishlist);
    void deleteByUserIdAndIsWishlist(Long userId, boolean isWishlist);
}
