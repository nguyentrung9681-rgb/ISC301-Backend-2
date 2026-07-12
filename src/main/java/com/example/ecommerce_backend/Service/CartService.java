package com.example.ecommerce_backend.Service;

import com.example.ecommerce_backend.Entity.Cart;
import com.example.ecommerce_backend.Entity.CartItem;
import com.example.ecommerce_backend.Entity.Product;
import com.example.ecommerce_backend.Entity.User;
import com.example.ecommerce_backend.Repository.CartItemRepository;
import com.example.ecommerce_backend.Repository.CartRepository;
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
    private CartRepository cartRepository;
    @Autowired
    private ProductRepository productRepository;

    public List<CartItem> getItems(User user, boolean isWishlist) {
        return cartItemRepository.findByUserIdAndIsWishlist(user.getId(), isWishlist);
    }

    public CartItem addItem(User user, Long productId, int quantity, String size, String color, boolean isWishlist) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new RuntimeException("San pham khong ton tai"));
        Cart cart = cartRepository.findByUserId(user.getId())
                .orElseGet(() -> cartRepository.save(new Cart(null, user)));

        Optional<CartItem> existing = cartItemRepository.findByUserIdAndProductIdAndSelectedSizeAndSelectedColorAndIsWishlist(
                user.getId(), productId, size, color, isWishlist);
        if (existing.isPresent()) {
            CartItem item = existing.get();
            item.setCart(cart);
            if (!isWishlist) {
                item.setQuantity(item.getQuantity() + quantity);
            }
            return cartItemRepository.save(item);
        }

        CartItem newItem = new CartItem();
        newItem.setCart(cart);
        newItem.setUser(user);
        newItem.setProduct(product);
        newItem.setQuantity(isWishlist ? 1 : quantity);
        newItem.setSelectedSize(size);
        newItem.setSelectedColor(color);
        newItem.setWishlist(isWishlist);
        return cartItemRepository.save(newItem);
    }

    public CartItem updateQuantity(Long cartItemId, int quantity) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new RuntimeException("Khong tim thay muc trong gio"));
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    public void deleteItem(Long cartItemId) {
        cartItemRepository.deleteById(cartItemId);
    }
}
