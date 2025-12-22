package com.junevi.baotao.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartDtos {

    @Getter
    @Setter
    public static class AddCartItemRequest {
        private Long productId;
        private int quantity;
    }

    @Getter
    @Setter
    public static class UpdateCartItemRequest {
        private int quantity;
    }
}


