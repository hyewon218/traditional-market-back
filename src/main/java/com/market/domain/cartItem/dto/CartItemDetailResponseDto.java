package com.market.domain.cartItem.dto;

import com.market.domain.cartItem.entity.CartItem;
import com.market.domain.image.dto.ImageResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@AllArgsConstructor
@Setter
public class CartItemDetailResponseDto {

    private Long cartItemNo; //장바구니 상품 No

    private Long itemNo; //상품 no

    private String itemName; //상품명

    private int price; //상품 금액

    private int initialCount; //수량

    private List<ImageResponseDto> imageList; // 상품 이미지

    public static CartItemDetailResponseDto of(CartItem cartItem) {
        return CartItemDetailResponseDto.builder()
            .cartItemNo(cartItem.getNo())
            .itemNo(cartItem.getItem().getNo())
            .itemName(cartItem.getItem().getItemName())
            .price(cartItem.getItem().getPrice())
            .initialCount(cartItem.getCount())
            .imageList(
                cartItem.getItem().getImageList().stream().map(ImageResponseDto::of).toList())
            .build();
    }
}