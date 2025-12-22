package com.junevi.baotao.controller;

import com.junevi.baotao.domain.CartItem;
import com.junevi.baotao.domain.User;
import com.junevi.baotao.dto.CartDtos;
import com.junevi.baotao.service.CartService;
import com.junevi.baotao.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/cart")
public class CartController {

    private final CartService cartService;
    private final UserService userService;

    public CartController(CartService cartService, UserService userService) {
        this.cartService = cartService;
        this.userService = userService;
    }

    private User currentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("当前用户不存在"));
    }

    @GetMapping
    public List<CartItem> list(Authentication authentication) {
        return cartService.listCartItems(currentUser(authentication));
    }

    @PostMapping("/items")
    public CartItem addItem(@Valid @RequestBody CartDtos.AddCartItemRequest request,
                            Authentication authentication) {
        return cartService.addToCart(currentUser(authentication), request.getProductId(), request.getQuantity());
    }

    @PutMapping("/items/{id}")
    public CartItem updateItem(@PathVariable("id") Long id,
                               @Valid @RequestBody CartDtos.UpdateCartItemRequest request,
                               Authentication authentication) {
        return cartService.updateQuantity(currentUser(authentication), id, request.getQuantity());
    }

    @DeleteMapping("/items/{id}")
    public ResponseEntity<?> removeItem(@PathVariable("id") Long id,
                                        Authentication authentication) {
        cartService.removeItem(currentUser(authentication), id);
        return ResponseEntity.noContent().build();
    }
}


