package com.market.domain.cart.dto;

import com.market.domain.cart.entity.Cart;
import com.market.domain.member.entity.Member;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CartRequestDto {

    private Member member;

    public static Cart toEntity(Member member) {
        return Cart.builder()
            .member(member)
            .build();
    }
}