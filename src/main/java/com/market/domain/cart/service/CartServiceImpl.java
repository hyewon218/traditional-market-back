package com.market.domain.cart.service;

import com.market.domain.cart.dto.CartRequestDto;
import com.market.domain.cartItem.dto.CartItemOrderRequestDto;
import com.market.domain.cart.entity.Cart;
import com.market.domain.cart.repository.CartRepository;
import com.market.domain.cartItem.dto.CartItemRequestDto;
import com.market.domain.cartItem.entity.CartItem;
import com.market.domain.cartItem.repository.CartItemRepository;
import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderRepository;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import com.market.domain.orderItem.entity.OrderItem;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final ItemRepository itemRepository;
    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;

    @Override
    @Transactional
    public Long addCart(CartItemRequestDto cartItemDto, Member member) {
        // 선택한 상품 장바구니
        Item item = itemRepository.findById(cartItemDto.getItemNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
        );
        Cart cart = getCartByMemberNo(member.getMemberNo());

        if (cart == null) {
            cart = CartRequestDto.toEntity(member);
            cartRepository.save(cart); // 장바구니 없으면 생성
        }
        CartItem savedCartItem = cartItemRepository.findByCart_NoAndItem_No(cart.getNo(), item.getNo());

        if (savedCartItem != null) {
            savedCartItem.addCount(cartItemDto.getCount()); // 장바구니에 담겨있는 상품이라면 갯수 더함
            return savedCartItem.getNo();
        } else {
            CartItem cartItem = cartItemDto.toEntity(cart, item);
            cartItemRepository.save(cartItem);
            return cartItem.getNo();
        }
    }

    @Override
    @Transactional
    public Long cartOrders(List<OrderItemRequestDto> orderItemDtoList, Member member) {
        List<OrderItem> orderItemList = new ArrayList<>(); // 주문 상품 담는 리스트

        for (OrderItemRequestDto orderItemDto : orderItemDtoList) {
            // 선택한 상품 주문
            Item item = itemRepository.findById(orderItemDto.getItemNo()).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
            );
            orderItemList.add(orderItemDto.toEntity(item)); // (상품 담아) 주문 상품 생성
        }
        Order order = Order.toEntity(member, orderItemList); // (주문 상품 담아) 주문 생성
        orderRepository.save(order); // 주문 저장
        return order.getNo();
    }

    @Override
    @Transactional
    public Long orderCartItems(List<CartItemOrderRequestDto> cartOrderDtoList, Member member) {
        List<OrderItemRequestDto> orderItemList = new ArrayList<>(); // 주문 상품 담는 리스트

        for (CartItemOrderRequestDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                .orElseThrow(
                    () -> new BusinessException(ErrorCode.NOT_FOUND_CART_ITEM)
                );
            // 장바구니상품에서 주문상품 정보 가져오기
            OrderItemRequestDto orderDto = OrderItemRequestDto.of(cartItem);
            orderItemList.add(orderDto);

        }
            Long orderId = cartOrders(orderItemList, member); // 장바구니에 담긴 상품들 주문

        for (CartItemOrderRequestDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemId())
                .orElseThrow(
                    () -> new BusinessException(ErrorCode.NOT_FOUND_CART_ITEM)
                );
            cartItemRepository.delete(cartItem);
        }
        return orderId;
    }

    public Cart getCartByMemberNo(Long memberNo) { // memberNo 로 장바구니 찾기
        return cartRepository.findByMember_MemberNo(memberNo);
    }
}