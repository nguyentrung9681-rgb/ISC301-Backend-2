package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.CartItem;
import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Repository.CartItemRepository;
import com.example.ecommerce_backend.Repository.ProductRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CartService {
    @Autowired
    private CartItemRepository cartItemRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<CartItem> getItems(User user, boolean isWishlist) {
        return cartItemRepository.findByUserIdAndIsWishlist(user.getId(), isWishlist);
    }

    public CartItem addItem(User user, Long productId, int quantity, boolean isWishlist) {
        Product product = productRepository.findById(productId).orElseThrow(()-> new RuntimeException("Sản phẩm không tồn tại"));
        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductIdAndIsWishlist(user.getId(), productId, isWishlist);
        if(existing.isPresent()){
            CartItem item = existing.get();
            if(!isWishlist) {
                item.setQuantity(item.getQuantity() + quantity);
            }
            return cartItemRepository.save(item);
        }

        CartItem newItem = new CartItem(null, user, product, isWishlist ? 1: quantity, isWishlist);
        return cartItemRepository.save(newItem);
    }

    public CartItem updateQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId).orElseThrow(()-> new RuntimeException("Không tìm thấy mục trong giỏ"));
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    public void deleteItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}
