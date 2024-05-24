package com.market.domain.order.service;

import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderRepository;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import com.market.domain.orderItem.entity.OrderItem;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;

    @Override
    @Transactional
    public Long order(OrderItemRequestDto orderItemDto, Member member) {
        // 선택한 상품 주문
        Item item = itemRepository.findById(orderItemDto.getItemNo()).orElseThrow(
            () -> new BusinessException(ErrorCode.NOT_FOUND_ITEM)
        );
        List<OrderItem> orderItemList = new ArrayList<>(); // 주문 상품 담는 리스트
        orderItemList.add(orderItemDto.toEntity(item)); // (상품 담아) 주문 상품 생성
        Order order = Order.toEntity(member, orderItemList); // (주문 상품 담아) 주문 생성
        orderRepository.save(order); // 주문 저장
        return order.getNo();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<OrderHistResponseDto> getOrderList(Member member, int page, int size, String sortBy,
        boolean isAsc) {
        Direction direction = isAsc ? Direction.DESC : Direction.ASC;
        Pageable pageable = PageRequest.of(page, size, Sort.by(direction, sortBy));
        // 회원 및 주문 데이터 조회
        Page<Order> orderList = orderRepository.findOrderListWithMember(member.getMemberId(),
            pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    @Override // 주문 취소
    @Transactional
    public void cancelOrder(Long orderNo, Member member) {
        Order order = findOrder(orderNo);
        if (validateOrder(orderNo, member)) {
            order.cancelOrder();
        }
    }

    @Override // 주문 검증
    @Transactional
    public boolean validateOrder(Long orderNo, Member member) {
        Order order = findOrder(orderNo);

        if (!member.getMemberId().equals(order.getMember().getMemberId())) {
            throw new BusinessException(ErrorCode.NOT_ORDER_DELETE);
        }
        return true;
    }

    @Override // 주문 찾기
    @Transactional
    public Order findOrder(Long orderNo) {
        return orderRepository.findByOrderNoWithMemberAndOrderItemListAndItem(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
    }
}
