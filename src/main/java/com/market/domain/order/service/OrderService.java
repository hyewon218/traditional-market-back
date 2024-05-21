package com.market.domain.order.service;

import com.market.domain.member.entity.Member;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.entity.Order;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import org.springframework.data.domain.Page;

public interface OrderService {

    /**
     * 주문 생성
     *
     * @param requestDto : 주문 생성 요청정보
     * @return : 주문번호
     */
    Long order(OrderItemRequestDto requestDto, Member member);

    /**
     * 주문 목록 조회
     *
     * @return : 조회된 주문들 정보
     */
    Page<OrderHistResponseDto> getOrderList(Member member, int page, int size, String sortBy,
        boolean isAsc);

    /**
     * 주문 취소
     */
    void cancelOrder(Long orderNo, Member member);

    /**
     * 주문 취소 시 검증
     */
    boolean validateOrder(Long orderNo, Member member);

    /**
     * 주문 찾기
     *
     * @return : 조회된 주문들 정보
     */
    Order findOrder(Long orderNo);
}
