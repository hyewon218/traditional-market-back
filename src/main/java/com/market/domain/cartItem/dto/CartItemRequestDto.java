package com.market.domain.cartItem.dto;

import com.market.domain.cart.entity.Cart;
import com.market.domain.cartItem.entity.CartItem;
import com.market.domain.item.entity.Item;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemRequestDto {

    @NotNull(message = "상품 아이디는 필수 입력 값 입니다.")
    private Long itemNo;

    @Min(value = 1, message = "최소 1개 이상 담아주세요.")
    private int count;

    public CartItem toEntity(Cart cart, Item item) {
        return CartItem.builder()
            .cart(cart)
            .item(item)
            .count(this.count)
            .build();
    }
}