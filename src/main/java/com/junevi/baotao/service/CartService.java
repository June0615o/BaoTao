package com.junevi.baotao.service;

import com.junevi.baotao.domain.CartItem;
import com.junevi.baotao.domain.Product;
import com.junevi.baotao.domain.User;
import com.junevi.baotao.repository.CartItemRepository;
import com.junevi.baotao.repository.ProductRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CartService {

    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;

    public CartService(CartItemRepository cartItemRepository, ProductRepository productRepository) {
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
    }

    public List<CartItem> listCartItems(User user) {
        return cartItemRepository.findByUser(user);
    }

    @Transactional
    public CartItem addToCart(User user, Long productId, int quantity) {
        Product product = productRepository.findById(productId)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于 0");
        }

        CartItem item = cartItemRepository.findByUserAndProductId(user, productId)
                .orElseGet(CartItem::new);
        item.setUser(user);
        item.setProduct(product);
        int newQty = (item.getQuantity() == null ? 0 : item.getQuantity()) + quantity;
        item.setQuantity(newQty);
        return cartItemRepository.save(item);
    }

    @Transactional
    public CartItem updateQuantity(User user, Long cartItemId, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于 0");
        }
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("购物车条目不存在"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权修改该购物车条目");
        }
        item.setQuantity(quantity);
        return cartItemRepository.save(item);
    }

    @Transactional
    public void removeItem(User user, Long cartItemId) {
        CartItem item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new IllegalArgumentException("购物车条目不存在"));
        if (!item.getUser().getId().equals(user.getId())) {
            throw new IllegalArgumentException("无权删除该购物车条目");
        }
        cartItemRepository.delete(item);
    }

    @Transactional
    public void clearCart(User user) {
        cartItemRepository.deleteByUser(user);
    }
}


