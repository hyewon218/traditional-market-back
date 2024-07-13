package com.market.domain.order.service;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderRepository;
import com.market.domain.orderItem.dto.OrderItemHistResponseDto;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import com.market.domain.orderItem.entity.OrderItem;
import com.market.domain.orderItem.repository.OrderItemRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final ItemRepository itemRepository;
    private final OrderItemRepository orderItemRepository;

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
    public Page<OrderHistResponseDto> getOrderList(Member member, Pageable pageable) {
        // 회원 및 주문 데이터 조회
        Page<Order> orderList = orderRepository.findOrderListWithMember(member.getMemberId(),
            pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderItemHistResponseDto> getOrderItemList(Member member) {
        Order order = getFirstOrderByMemberNo(member); // 로그인한 member 정보로 가장 최근 order 정보 가져오기

        List<OrderItemHistResponseDto> orderItemDtoList = orderItemRepository.findOrderItemHistResponseDtoList(
            order.getNo(), member.getMemberNo());

        // 각 DTO 에 이미지 목록 추가
        for (OrderItemHistResponseDto orderItemDto : orderItemDtoList) {
            // 이미지 목록을 얻기 위해 엔터티를 가져옴
            OrderItem orderItem = orderItemRepository.findById(orderItemDto.getOrderItemNo())
                .orElseThrow();
            List<ImageResponseDto> imageList = orderItem.getItem().getImageList().stream()
                .map(ImageResponseDto::of)
                .toList();
            orderItemDto.setImageList(imageList); // DTO 에 이미지 추가
        }
        return orderItemDtoList;
    }

    @Override
    @Transactional
    public Order getFirstOrderByMemberNo(Member member) { // member 로 가장 최근 주문 찾기
        return orderRepository.findFirstByMemberOrderByCreateTimeDesc(member)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
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
