package com.market.domain.cartItem.service;

import com.market.domain.cartItem.dto.CartItemDetailResponseDto;
import com.market.domain.cartItem.dto.CartItemRequestDto;
import com.market.domain.cartItem.entity.CartItem;
import com.market.domain.member.entity.Member;
import java.util.List;

public interface CartItemService {
    CartItem getCartItemById(Long cartItemId);

    List<CartItemDetailResponseDto> getCartItemList(Member member);

    void updateCartItemCount(Long cartItemId, CartItemRequestDto cartItemRequestDto,
        Member member);

    void deleteCartItem(Long cartItemId, Member member);

    boolean validateCartItem(Long cartItemId, Member member);
}