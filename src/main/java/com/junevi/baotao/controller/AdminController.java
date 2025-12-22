package com.junevi.baotao.controller;

import com.junevi.baotao.domain.*;
import com.junevi.baotao.dto.ProductDtos;
import com.junevi.baotao.repository.UserRepository;
import com.junevi.baotao.service.BrowseLogService;
import com.junevi.baotao.service.OrderService;
import com.junevi.baotao.service.ProductService;
import com.junevi.baotao.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.*;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final ProductService productService;
    private final OrderService orderService;
    private final UserService userService;
    private final BrowseLogService browseLogService;
    private final UserRepository userRepository;

    public AdminController(ProductService productService,
                           OrderService orderService,
                           UserService userService,
                           BrowseLogService browseLogService,
                           UserRepository userRepository) {
        this.productService = productService;
        this.orderService = orderService;
        this.userService = userService;
        this.browseLogService = browseLogService;
        this.userRepository = userRepository;
    }

    // 商品管理

    @GetMapping("/products")
    public Page<Product> listProducts(@RequestParam(value = "q", required = false) String keyword,
                                      @RequestParam(value = "page", defaultValue = "0") int page,
                                      @RequestParam(value = "size", defaultValue = "10") int size) {
        return productService.listProducts(keyword, page, size);
    }

    @PostMapping("/products")
    public Product createProduct(@RequestBody ProductDtos.ProductRequest request) {
        Product product = new Product();
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCoverImageUrl(request.getCoverImageUrl());
        product.setCategory(request.getCategory());
        return productService.save(product);
    }

    @PutMapping("/products/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable("id") Long id,
                                                 @RequestBody ProductDtos.ProductRequest request) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));
        product.setName(request.getName());
        product.setDescription(request.getDescription());
        product.setPrice(request.getPrice());
        product.setStock(request.getStock());
        product.setCoverImageUrl(request.getCoverImageUrl());
        product.setCategory(request.getCategory());
        return ResponseEntity.ok(productService.save(product));
    }

    @DeleteMapping("/products/{id}")
    public ResponseEntity<?> deleteProduct(@PathVariable("id") Long id) {
        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    // 订单管理

    @GetMapping("/orders")
    public Page<Order> listOrders(@RequestParam(value = "status", required = false) OrderStatus status,
                                  @RequestParam(value = "page", defaultValue = "0") int page,
                                  @RequestParam(value = "size", defaultValue = "10") int size) {
        if (status != null) {
            return orderService.listOrdersByStatus(status, page, size);
        }
        return orderService.listAllOrders(page, size);
    }

    @PutMapping("/orders/{id}/status")
    public ResponseEntity<Order> updateOrderStatus(@PathVariable("id") Long id,
                                                   @RequestParam("status") OrderStatus status) {
        return ResponseEntity.ok(orderService.updateStatus(id, status));
    }

    // 简单销售统计：按日期汇总订单金额

    @GetMapping("/statistics/sales-by-day")
    public List<Map<String, Object>> salesByDay() {
        List<Order> allOrders = orderService.listAllOrders(0, Integer.MAX_VALUE).getContent();
        Map<LocalDate, BigDecimal> map = new TreeMap<LocalDate, BigDecimal>();
        for (Order order : allOrders) {
            Date date = Date.from(order.getCreatedAt().atZone(ZoneId.systemDefault()).toInstant());
            LocalDate day = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
            BigDecimal total = map.containsKey(day) ? map.get(day) : BigDecimal.ZERO;
            total = total.add(order.getTotalAmount());
            map.put(day, total);
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map.Entry<LocalDate, BigDecimal> entry : map.entrySet()) {
            Map<String, Object> row = new HashMap<String, Object>();
            row.put("date", entry.getKey().toString());
            row.put("amount", entry.getValue());
            result.add(row);
        }
        return result;
    }

    // 简单统计：销量 Top N 商品

    @GetMapping("/statistics/top-products")
    public List<Map<String, Object>> topProducts(@RequestParam(value = "limit", defaultValue = "5") int limit) {
        List<Order> allOrders = orderService.listAllOrders(0, Integer.MAX_VALUE).getContent();
        Map<Long, Integer> countMap = new HashMap<Long, Integer>();
        Map<Long, String> nameMap = new HashMap<Long, String>();
        for (Order order : allOrders) {
            if (order.getItems() == null) {
                continue;
            }
            for (OrderItem item : order.getItems()) {
                if (item.getProduct() == null) {
                    continue;
                }
                Long pid = item.getProduct().getId();
                int qty = countMap.containsKey(pid) ? countMap.get(pid) : 0;
                qty += item.getQuantity();
                countMap.put(pid, qty);
                if (!nameMap.containsKey(pid)) {
                    nameMap.put(pid, item.getProduct().getName());
                }
            }
        }
        List<Map<String, Object>> result = new ArrayList<Map<String, Object>>();
        for (Map.Entry<Long, Integer> entry : countMap.entrySet()) {
            Map<String, Object> row = new HashMap<String, Object>();
            Long pid = entry.getKey();
            row.put("productId", pid);
            row.put("productName", nameMap.get(pid));
            row.put("quantity", entry.getValue());
            result.add(row);
        }
        Collections.sort(result, new java.util.Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                Integer q1 = (Integer) o1.get("quantity");
                Integer q2 = (Integer) o2.get("quantity");
                return q2.compareTo(q1);
            }
        });
        if (result.size() > limit) {
            return result.subList(0, limit);
        }
        return result;
    }

    // 客户及其日志

    @GetMapping("/customers")
    public java.util.List<User> listCustomers() {
        return userService.findAll();
    }

    @GetMapping("/customers/{id}/logs")
    public ResponseEntity<java.util.List<BrowseLog>> logs(@PathVariable("id") Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(new java.util.function.Supplier<IllegalArgumentException>() {
                    @Override
                    public IllegalArgumentException get() {
                        return new IllegalArgumentException("用户不存在");
                    }
                });
        return ResponseEntity.ok(browseLogService.listLogsForUser(user));
    }
}


