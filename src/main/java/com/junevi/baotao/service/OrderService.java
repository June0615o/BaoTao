package com.junevi.baotao.service;

import com.junevi.baotao.domain.*;
import com.junevi.baotao.repository.CartItemRepository;
import com.junevi.baotao.repository.OrderItemRepository;
import com.junevi.baotao.repository.OrderRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final MailService mailService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository,
                        MailService mailService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.mailService = mailService;
    }

    public Page<Order> listOrdersForUser(User user, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByUser(user, pageable);
    }

    public Page<Order> listAllOrders(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findAll(pageable);
    }

    public Page<Order> listOrdersByStatus(OrderStatus status, int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return orderRepository.findByStatus(status, pageable);
    }

    public Order getOrderById(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("订单不存在"));
    }

    @Transactional
    public Order checkout(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("购物车为空");
        }

        Order order = new Order();
        order.setUser(user);
        order.setStatus(OrderStatus.PAID);

        List<OrderItem> orderItems = new ArrayList<OrderItem>();
        BigDecimal total = BigDecimal.ZERO;
        for (CartItem cartItem : cartItems) {
            OrderItem item = new OrderItem();
            item.setOrder(order);
            item.setProduct(cartItem.getProduct());
            item.setQuantity(cartItem.getQuantity());
            item.setUnitPrice(cartItem.getProduct().getPrice());
            orderItems.add(item);

            BigDecimal line = cartItem.getProduct().getPrice()
                    .multiply(BigDecimal.valueOf(cartItem.getQuantity()));
            total = total.add(line);
        }
        order.setTotalAmount(total);
        order = orderRepository.save(order);
        for (OrderItem item : orderItems) {
            item.setOrder(order);
        }
        orderItemRepository.saveAll(orderItems);

        cartItemRepository.deleteByUser(user);

        // 简单发送邮件通知（如果配置了 SMTP）
        mailService.sendOrderConfirmMail(user.getEmail(),
                "订单确认",
                "您的订单已生成，金额为：" + total.toPlainString());
        return order;
    }

    @Transactional
    public Order updateStatus(Long orderId, OrderStatus status) {
        Order order = getOrderById(orderId);
        order.setStatus(status);
        if (status == OrderStatus.PAID && order.getPaidAt() == null) {
            order.setPaidAt(java.time.LocalDateTime.now());
        }
        return orderRepository.save(order);
    }
}


