package com.market.domain.cartItem.dto;

import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartItemOrderRequestDto { // 장바구니 상품 주문

    private Long cartItemId;

    @NotBlank(message = "주문할 상품을 선택해 주세요.")
    private List<CartItemOrderRequestDto> cartOrderDtoList;
}