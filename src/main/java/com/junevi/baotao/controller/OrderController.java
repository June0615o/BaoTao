package com.junevi.baotao.controller;

import com.junevi.baotao.domain.Order;
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
/**
 * 订单接口：查询订单、查看订单详情、购物车结算生成订单等。
 */
public class OrderController {

    private final OrderService orderService;
    private final UserService userService;

    public OrderController(OrderService orderService,
                           UserService userService,
                           BrowseLogService browseLogService) {
        this.orderService = orderService;
        this.userService = userService;
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
    /**
     * 从当前登录用户的购物车结算下单。
     *
     * <p>失败场景：</p>
     * <ul>
     *   <li>购物车为空：409</li>
     *   <li>库存不足：409</li>
     * </ul>
     */
    public ResponseEntity<Order> checkout(Authentication authentication) {
        User user = currentUser(authentication);
        Order order = orderService.checkout(user);
        return ResponseEntity.ok(order);
    }
}


