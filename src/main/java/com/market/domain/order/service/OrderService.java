package com.market.domain.order.service;

import com.market.domain.member.entity.Member;
import com.market.domain.order.dto.SaveDeliveryRequestDto;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.entity.Order;
import com.market.domain.orderItem.dto.OrderItemHistResponseDto;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderService {

    /**
     * 주문 생성
     *
     * @param requestDto : 주문 생성 요청정보
     * @return : 주문번호
     */
    Long order(OrderItemRequestDto requestDto, Member member);

    /**
     * 가장 최근 주문 찾기 (주문페이지)
     *
     * @param member : 로그인한 사용자
     */
    Order getFirstOrderByMemberNo(Member member);

    /**
     * 주문 내 상품 목록 조회
     *
     * @return : 조회된 주문들 정보
     */
    List<OrderItemHistResponseDto> getOrderItemList(Member member);

    /**
     * 결제 요청 시 배송지 저장
     */
    void setDeliveryAddr(Member member, SaveDeliveryRequestDto saveDeliveryRequestDto);

    /**
     * 가장 최근 COMPLETE 주문 조회
     *
     * @return : 조회된 주문들 정보
     */
    OrderHistResponseDto getLatestOrder(Member member);

    /**
     * COMPLETE 주문 목록 조회
     *
     * @return : 조회된 주문들 정보
     */
    Page<OrderHistResponseDto> getOrders(Member member, Pageable pageable);

    /*결제 승인 후*/
    /**
     * order 의 status COMPLETE 로 변경
     */
    void setOrderComplete(Order order);

    /**
     * ORDER 주문 목록 조회
     *
     * @return : 조회된 주문 목록
     */
    Order getStatusOrder(Member member);

    /**
     * ORDER 주문 목록 조회(전체)
     *
     * @return : 조회된 주문 목록
     */

    List<Order> getAllStatusOrders();

    /**
     * 주문 상태 ORDER 인 주문 목록 삭제(전체)
     **/

    void deleteAllStatusOrders();

    /**
     * 주문 취소
     */
    void cancelOrder(Long orderNo, Member member);

    /**
     * 주문 취소 시 검증
     */
    void validateOrder(Long orderNo, Member member);

    /**
     * 특정 주문 조회
     *
     * @return : 조회된 주문 정보
     */
    Order findById(Long orderNo);
}
