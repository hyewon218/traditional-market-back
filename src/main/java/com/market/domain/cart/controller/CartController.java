package com.market.domain.cart.controller;

import com.market.domain.cartItem.dto.CartItemDetailResponseDto;
import com.market.domain.cartItem.dto.CartItemOrderRequestDto;
import com.market.domain.cart.service.CartService;
import com.market.domain.cartItem.dto.CartItemRequestDto;
import com.market.domain.cartItem.service.CartItemService;
import com.market.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class CartController {

    private final CartService cartService;
    private final CartItemService cartItemService;

    @PostMapping(value = "/carts") // 장바구니에 추가
    public ResponseEntity<Long> addOrModify(@RequestBody CartItemRequestDto cartItemDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long cartItemId = cartService.addCart(cartItemDto, userDetails.getMember());
        return ResponseEntity.ok().body(cartItemId);
    }

    @PostMapping(value = "/carts/order") // 장바구니 상품 주문
    public ResponseEntity<Long> orderCartItems(
        @RequestBody List<CartItemOrderRequestDto> cartOrderDtoList,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        Long orderNo = cartService.orderCartItems(cartOrderDtoList, userDetails.getMember());
        return ResponseEntity.ok().body(orderNo);
    }

    @GetMapping(value = "/cartitems") // 장바구니 내 상품 목록 조회
    public ResponseEntity<List<CartItemDetailResponseDto>> getCartItemList(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(cartItemService.getCartItemList(userDetails.getMember()));
    }

    @PatchMapping(value = "/cartitems/{cartItemNo}") // 장바구니 특정 상품 주문 수 수정
    public ResponseEntity<Long> updateCartItem(@RequestBody CartItemRequestDto cartItemRequestDto,
        @PathVariable("cartItemNo") Long cartItemNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        cartItemService.updateCartItemCount(cartItemNo, cartItemRequestDto,
            userDetails.getMember());
        return ResponseEntity.ok().body(cartItemNo);
    }

    @DeleteMapping(value = "/cartitems/{cartItemNo}") // 장바구니 특정 상품 주문 삭제
    public ResponseEntity<Long> deleteCartItem(@PathVariable("cartItemNo") Long cartItemNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        cartItemService.deleteCartItem(cartItemNo, userDetails.getMember());
        return ResponseEntity.ok().body(cartItemNo);
    }
}