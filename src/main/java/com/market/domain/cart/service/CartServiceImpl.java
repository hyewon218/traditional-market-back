package com.market.domain.cart.service;

import com.market.domain.cart.dto.CartRequestDto;
import com.market.domain.cart.entity.Cart;
import com.market.domain.cart.repository.CartRepository;
import com.market.domain.cartItem.dto.CartItemOrderRequestDto;
import com.market.domain.cartItem.dto.CartItemRequestDto;
import com.market.domain.cartItem.entity.CartItem;
import com.market.domain.cartItem.repository.CartItemRepository;
import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderRepository;
import com.market.domain.order.service.OrderService;
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
    private final OrderService orderService;

    @Override
    @Transactional // 장바구니 추가
    public Long addCart(CartItemRequestDto cartItemDto, Member member) {
        // 선택한 상품 장바구니
        Item item = itemRepository.findById(cartItemDto.getItemNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
        );
        Cart cart = getOrCreateCartByMember(member);
        CartItem savedCartItem = cartItemRepository.findByCart_NoAndItem_No(cart.getNo(),
            item.getNo());

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
    @Transactional // 장바구니 상품 주문
    public Long cartOrders(List<OrderItemRequestDto> orderItemDtoList, Member member) {

        // 이전 주문(결제하지 않은, 주문 상태 ORDER) 이 있는지 확인
        Order existOrder = orderService.getStatusOrder(member);
        if (existOrder != null) { // 이전 주문이 있으면 삭제
            orderRepository.delete(existOrder);
        }

        List<OrderItem> orderItemList = new ArrayList<>(); // 주문 상품 담는 리스트
        for (OrderItemRequestDto orderItemDto : orderItemDtoList) {
            // 선택한 상품 주문
            Item item = itemRepository.findByIdWithLock(orderItemDto.getItemNo()).orElseThrow(
                () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
            );
            orderItemList.add(orderItemDto.toEntity(item)); // (상품 담아) 주문 상품 생성
        }
        Order order = Order.toEntity(member, orderItemList, true); // (주문 상품 담아) 주문 생성
        orderRepository.save(order); // 주문 저장
        return order.getNo();
    }

    @Override
    @Transactional
    public Long orderCartItems(List<CartItemOrderRequestDto> cartOrderDtoList, Member member) {
        List<OrderItemRequestDto> orderItemList = new ArrayList<>(); // 주문 상품 담는 리스트
        for (CartItemOrderRequestDto cartOrderDto : cartOrderDtoList) {
            CartItem cartItem = cartItemRepository.findById(cartOrderDto.getCartItemNo())
                .orElseThrow(
                    () -> new BusinessException(ErrorCode.NOT_FOUND_CART_ITEM)
                );
            // 장바구니상품에서 주문상품 정보 가져오기
            OrderItemRequestDto orderDto = OrderItemRequestDto.of(cartItem);
            orderItemList.add(orderDto);
        }
        return cartOrders(orderItemList, member);
    }

    public Cart getOrCreateCartByMember(Member member) { // member 로 장바구니 조회 및 생성
        return cartRepository.findByMember_MemberNo(member.getMemberNo())
            .orElseGet(() -> {
                Cart newCart = CartRequestDto.toEntity(member);
                cartRepository.save(newCart); // 장바구니 없으면 생성
                return newCart;
            });
    }
}