package com.junevi.baotao.service;

import com.junevi.baotao.domain.*;
import com.junevi.baotao.repository.CartItemRepository;
import com.junevi.baotao.repository.OrderItemRepository;
import com.junevi.baotao.repository.OrderRepository;
import com.junevi.baotao.repository.ProductRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
/**
 * 订单业务：下单结算、订单查询、订单状态更新等。
 *
 * <p>结算（checkout）必须在事务中完成，确保：扣库存、写订单、清空购物车要么全部成功，要么全部回滚。</p>
 */
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductRepository productRepository;
    private final MailService mailService;
    private final BrowseLogService browseLogService;

    public OrderService(OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        CartItemRepository cartItemRepository,
                        ProductRepository productRepository,
                        MailService mailService,
                        BrowseLogService browseLogService) {
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.cartItemRepository = cartItemRepository;
        this.productRepository = productRepository;
        this.mailService = mailService;
        this.browseLogService = browseLogService;
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
    /**
     * 从用户购物车结算生成订单。
     *
     * <p>关键点：</p>
     * <ul>
     *   <li>先按商品汇总数量，并使用“带条件的原子更新”扣减库存，避免并发超卖。</li>
     *   <li>库存不足时抛出 {@link IllegalStateException}，由全局异常处理返回 409(CONFLICT)。</li>
     *   <li>事务内完成写订单、写明细、清空购物车。</li>
     * </ul>
     */
    public Order checkout(User user) {
        List<CartItem> cartItems = cartItemRepository.findByUser(user);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("购物车为空");
        }

        // 先按商品汇总数量并原子扣减库存（避免并发超卖）
        Map<Long, Integer> qtyByProductId = new HashMap<Long, Integer>();
        for (CartItem cartItem : cartItems) {
            if (cartItem.getQuantity() == null || cartItem.getQuantity() <= 0) {
                throw new IllegalArgumentException("购物车商品数量不合法");
            }
            if (cartItem.getProduct() == null || cartItem.getProduct().getId() == null) {
                throw new IllegalStateException("购物车商品不存在");
            }
            Long pid = cartItem.getProduct().getId();
            int current = qtyByProductId.containsKey(pid) ? qtyByProductId.get(pid) : 0;
            qtyByProductId.put(pid, current + cartItem.getQuantity());
        }
        for (Map.Entry<Long, Integer> entry : qtyByProductId.entrySet()) {
            Long productId = entry.getKey();
            Integer qty = entry.getValue();
            Product product = productRepository.findById(productId)
                    .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
            int updated = productRepository.decreaseStockIfEnough(productId, qty);
            if (updated == 0) {
                throw new IllegalStateException("库存不足：" + product.getName());
            }
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

            // 记录购买日志
            browseLogService.logBuy(user, cartItem.getProduct());
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


