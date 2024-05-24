package com.market.domain.cartItem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDetailResponseDto {

    private Long cartItemId; //장바구니 상품 아이디

    private String itemName; //상품명

    private int price; //상품 금액

    private int count; //수량

    public CartItemDetailResponseDto(Long cartItemId, String itemName, int price, int count) {
        this.cartItemId = cartItemId;
        this.itemName = itemName;
        this.price = price;
        this.count = count;
    }
}