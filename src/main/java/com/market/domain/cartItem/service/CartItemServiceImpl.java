package com.market.domain.cartItem.service;

import com.market.domain.cart.entity.Cart;
import com.market.domain.cart.service.CartService;
import com.market.domain.cartItem.dto.CartItemDetailResponseDto;
import com.market.domain.cartItem.dto.CartItemRequestDto;
import com.market.domain.cartItem.entity.CartItem;
import com.market.domain.cartItem.repository.CartItemRepository;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.Collections;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartItemServiceImpl implements CartItemService {

    private final CartItemRepository cartItemRepository;
    private final CartService cartService;

    @Override
    @Transactional(readOnly = true)
    public List<CartItemDetailResponseDto> getCartItemList(Member member) {
        Cart cart = cartService.getCartByMemberNo(member.getMemberNo()); // 로그인한 member 정보로 cart 정보 가져오기
        if (cart == null) {
            return Collections.emptyList(); // cart 가 null(상품 추가 전)이면 빈 리스트 반환
        }
        return cartItemRepository.findCartDetailDtoList(cart.getNo());
    }

    @Override
    @Transactional
    public void updateCartItemCount(Long cartItemNo, CartItemRequestDto cartItemRequestDto,
        Member member) {
        CartItem cartItem = getCartItemById(cartItemNo);
        if (validateCartItem(cartItemNo, member)) {
            cartItem.updateCount(cartItemRequestDto.getCount());
        }
    }

    @Override
    @Transactional
    public void deleteCartItem(Long cartItemNo, Member member) {
        CartItem cartItem = getCartItemById(cartItemNo);
        if (validateCartItem(cartItemNo, member)) {
            cartItemRepository.delete(cartItem);
        }
    }

    @Override // 주문 검증
    @Transactional
    public boolean validateCartItem(Long cartItemNo, Member member) {
        CartItem cartItem = getCartItemById(cartItemNo);

        if (!member.getMemberId().equals(cartItem.getCart().getMember().getMemberId())) {
            throw new BusinessException(ErrorCode.NOT_AUTHORITY_CART_ITEM);
        }
        return true;
    }

    @Transactional(readOnly = true) // 장바구니 상품 찾기
    public CartItem getCartItemById(Long cartItemNo) {
        return cartItemRepository.findById(cartItemNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CART_ITEM));
    }
}