package com.market.domain.cartItem.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemDetailResponseDto {

    private Long cartItemNo; //장바구니 상품 No

    private Long itemNo; //상품 no

    private String itemName; //상품명

    private int price; //상품 금액

    private int initialCount; //수량

    private String imageUrl;

    public CartItemDetailResponseDto(Long cartItemNo, Long itemNo, String itemName, int price, int count, String imageUrl) {
        this.cartItemNo = cartItemNo;
        this.itemNo = itemNo;
        this.itemName = itemName;
        this.price = price;
        this.initialCount = count;
        this.imageUrl = imageUrl;
    }
}