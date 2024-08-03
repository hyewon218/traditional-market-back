package com.market.domain.cart.service;

import com.market.domain.cart.entity.Cart;
import com.market.domain.cartItem.dto.CartItemOrderRequestDto;
import com.market.domain.cartItem.dto.CartItemRequestDto;
import com.market.domain.member.entity.Member;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import java.util.List;

public interface CartService {

    /**
     * 장바구니 담기
     *
     * @param cartItemDto : 장바구니 상품 요청 dto
     * @param member      : 장바구니 주인
     */
    Long addCart(CartItemRequestDto cartItemDto, Member member);

    /**
     * 장바구니 담긴 주문상품들 주문
     *
     * @param orderItemDtoList : 장바구니 상품 요청 dto 리스트
     * @param member           : 장바구니 주인
     */
    Long cartOrders(List<OrderItemRequestDto> orderItemDtoList, Member member);

    /**
     * 장바구니 담긴 상품들 주문
     *
     * @param cartOrderDtoList : 장바구니 상품 요청 dto 리스트
     * @param member           : 장바구니 주인
     */
    Long orderCartItems(List<CartItemOrderRequestDto> cartOrderDtoList, Member member);

    /**
     * 로그인한 사용자 정보로 카트 조회 및 생성
     *
     * @param member : 로그인한 사용자
     */
    Cart getOrCreateCartByMember(Member member);
}