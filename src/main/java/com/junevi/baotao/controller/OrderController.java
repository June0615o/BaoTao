package com.junevi.baotao.controller;

import com.junevi.baotao.domain.Order;
import com.junevi.baotao.domain.Product;
import com.junevi.baotao.domain.User;
import com.junevi.baotao.service.BrowseLogService;
import com.junevi.baotao.service.OrderService;
import com.junevi.baotao.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;
    private final BrowseLogService browseLogService;

    public OrderController(OrderService orderService,
                           UserService userService,
                           BrowseLogService browseLogService) {
        this.orderService = orderService;
        this.userService = userService;
        this.browseLogService = browseLogService;
    }

    private User currentUser(Authentication authentication) {
        String username = authentication.getName();
        return userService.findByUsername(username)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("当前用户不存在");
                    }
                });
    }

    @GetMapping
    public Page<Order> list(Authentication authentication,
                            @RequestParam(value = "page", defaultValue = "0") int page,
                            @RequestParam(value = "size", defaultValue = "10") int size) {
        return orderService.listOrdersForUser(currentUser(authentication), page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Order> detail(@PathVariable("id") Long id, Authentication authentication) {
        Order order = orderService.getOrderById(id);
        User user = currentUser(authentication);
        if (!order.getUser().getId().equals(user.getId())) {
            return ResponseEntity.status(403).build();
        }
        return ResponseEntity.ok(order);
    }

    @PostMapping("/checkout")
    public ResponseEntity<Order> checkout(Authentication authentication) {
        User user = currentUser(authentication);
        Order order = orderService.checkout(user);
        // 为该订单中的商品记录购买日志
        for (Product product : order.getItems().stream().map(i -> i.getProduct()).toArray(Product[]::new)) {
            browseLogService.logBuy(user, product);
        }
        return ResponseEntity.ok(order);
    }
}


