package com.market.domain.order.service;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.item.entity.Item;
import com.market.domain.item.repository.ItemRepository;
import com.market.domain.member.entity.Member;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.dto.SaveDeliveryRequestDto;
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
    public Order getFirstOrderByMemberNo(Member member) { // 가장 최근 주문 찾기 (주문페이지)
        return orderRepository.findFirstByMemberOrderByCreateTimeDesc(member)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
    }

    @Override
    @Transactional(readOnly = true) // 주문 내 상품 목록 조회
    public List<OrderItemHistResponseDto> getOrderItemList(Member member) {
        Order order = getFirstOrderByMemberNo(member); // 로그인한 member 정보로 가장 최근 order 정보 가져오기

        List<OrderItemHistResponseDto> orderItemDtoList = orderItemRepository.findOrderItemHistResponseDtoList(
            order.getNo(), member.getMemberNo());

        // 각 DTO 에 이미지 목록 추가
        for (OrderItemHistResponseDto orderItemDto : orderItemDtoList) {
            // 이미지 목록을 얻기 위해 엔터티를 가져옴
            OrderItem orderItem = orderItemRepository.findById(orderItemDto.getOrderItemNo()).orElseThrow();
            List<ImageResponseDto> imageList = orderItem.getItem().getImageList().stream()
                .map(ImageResponseDto::of)
                .toList();
            orderItemDto.setImageList(imageList); // DTO 에 이미지 추가
        }
        return orderItemDtoList;
    }

    @Override
    @Transactional // 결제 요청 시 주문 테이블에 배송지 저장
    public void setDeliveryAddr(Member member, SaveDeliveryRequestDto saveDeliveryRequestDto) {
        Order order = getFirstOrderByMemberNo(member);
        order.setDelivery(saveDeliveryRequestDto);
    }

    @Override
    @Transactional(readOnly = true) // 가장 최근 COMPLETE 주문 조회 (결제 완료 후 주문 상세 정보 조회 시 사용)
    public OrderHistResponseDto findLatestOrder(Member member) {
        Order order = orderRepository.findLatestOrder(member.getMemberNo(),
            OrderStatus.COMPLETE);
        return OrderHistResponseDto.of(order);
    }

    @Override
    @Transactional(readOnly = true) // COMPLETE 주문 목록 조회
    public Page<OrderHistResponseDto> getOrders(Member member, Pageable pageable) {
        // 회원 및 주문 데이터 조회
        Page<Order> orderList = orderRepository.findOrdersByMemberWithPaging(member.getMemberNo(),
            OrderStatus.COMPLETE, pageable);
        return orderList.map(OrderHistResponseDto::of);
    }

    /*결제 승인 후*/
    @Override
    @Transactional // order 의 status COMPLETE 로 변경
    public void setOrderComplete(Order order) {
        order.setOrderComplete();
    }

    @Override
    @Transactional(readOnly = true) // ORDER 주문 목록 조회
    public List<Order> getStatusOrders(Member member) {
        return orderRepository.findStatusOrders(member.getMemberNo(), OrderStatus.ORDER);
    }

    @Override
    @Transactional // 주문 상태 ORDER 인 주문 목록 재고 증가 후 주문 목록 삭제
    public void statusOrderItemListAddStockAndDelete(Member member) {
        List<Order> orderList = getStatusOrders(member);
        for (Order order : orderList) {
            order.statusOrderAddStock();
        }
        orderRepository.deleteAll(orderList);
    }

    @Override
    @Transactional // 결제 승인 후
    public void afterPayApprove(Member member, Order order) {
        setOrderComplete(order);
        statusOrderItemListAddStockAndDelete(member);
    }

    @Override
    @Transactional // 주문 취소
    public void cancelOrder(Long orderNo, Member member) {
        Order order = findOrder(orderNo);
        validateOrder(orderNo, member);
        order.cancelOrder();
    }

    @Override
    @Transactional // 주문 검증
    public void validateOrder(Long orderNo, Member member) {
        boolean exists = orderRepository.existsByNoAndMember_MemberNo(orderNo,
            member.getMemberNo());
        if (!exists) {
            throw new BusinessException(ErrorCode.NOT_AUTHORITY_ORDER);
        }
    }

    @Override
    @Transactional(readOnly = true) // 주문 찾기
    public Order findOrder(Long orderNo) {
        return orderRepository.findOrderDetailsByNo(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
    }

    @Override
    @Transactional(readOnly = true) // 특정 주문 조회
    public Order findById(Long orderNo) {
        return orderRepository.findById(orderNo)
            .orElseThrow(() -> new BusinessException(ErrorCode.ORDER_NOT_EXISTS));
    }
}
