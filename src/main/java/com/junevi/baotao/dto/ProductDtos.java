package com.junevi.baotao.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class ProductDtos {

    @Getter
    @Setter
    public static class ProductRequest {
        private String name;
        private String description;
        private BigDecimal price;
        private Integer stock;
        private String coverImageUrl;
        private String category;
    }
}


