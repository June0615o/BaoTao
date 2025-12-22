package com.junevi.baotao.controller;

import com.junevi.baotao.domain.Product;
import com.junevi.baotao.domain.User;
import com.junevi.baotao.service.BrowseLogService;
import com.junevi.baotao.service.ProductService;
import com.junevi.baotao.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;
    private final BrowseLogService browseLogService;
    private final UserService userService;

    public ProductController(ProductService productService,
                             BrowseLogService browseLogService,
                             UserService userService) {
        this.productService = productService;
        this.browseLogService = browseLogService;
        this.userService = userService;
    }

    @GetMapping
    public Page<Product> list(@RequestParam(value = "q", required = false) String keyword,
                              @RequestParam(value = "page", defaultValue = "0") int page,
                              @RequestParam(value = "size", defaultValue = "10") int size) {
        return productService.listProducts(keyword, page, size);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> detail(@PathVariable Long id, Authentication authentication) {
        Product product = productService.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("商品不存在"));

        if (authentication != null) {
            String username = authentication.getName();
            User user = userService.findByUsername(username).orElse(null);
            browseLogService.logView(user, product);
        }
        return ResponseEntity.ok(product);
    }
}


