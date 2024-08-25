package com.market.domain.order.service;

import com.market.domain.member.entity.Member;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.dto.SaveDeliveryRequestDto;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderSearchCond;
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
     * 본인의 CANCEL 제외한 모든 주문상태 주문 목록 조회
     *
     * @param member : 본인
     * @return : 조회된 주문들 정보
     */
    Page<OrderHistResponseDto> getOrders(Member member, Pageable pageable);

    /**
     * 본인의 CANCEL 주문 목록 조회
     *
     * @param member : 본인
     * @return : 조회된 주문들 정보
     */
    Page<OrderHistResponseDto> getCancelOrders(Member member, Pageable pageable);

    /**
     * CANCEL 제외한 모든 주문 목록 조회(관리자만 가능)
     *
     * @return : 조회된 주문들 정보
     */
    Page<OrderHistResponseDto> getOrdersAdmin(Pageable pageable);

    /**
     * 주문 상태별 조회(관리자만 가능)
     *
     * @return : 조회된 주문들 정보
     */
    Page<OrderHistResponseDto> getOrderStatusAdmin(OrderStatus orderStatus, Pageable pageable);

    /**
     * CANCEL 제외한 판매자가 자신이 소유한 상점의 상품들에 대한 주문 목록 조회 (판매자만 가능)
     *
     * @param seller : 판매자
     * @return : 조회된 주문들 정보
     */
    Page<OrderHistResponseDto> getOrdersSeller(Member seller, Pageable pageable);

    /**
     * 판매자가 자신이 소유한 상점의 상품들에 대한 주문 상태별 조회 (판매자만 가능)
     *
     * @param seller : 판매자
     * @param orderStatus : 조회할 주문 상태
     * @return : 조회된 주문들 정보
     */
    Page<OrderHistResponseDto> getCancelOrdersSeller(Member seller, OrderStatus orderStatus, Pageable pageable);

    /**
     * 주문 목록 검색 (관리자 주문 관리 페이지에서 사용)
     *
     * @param cond 조건
     * @return 검색한 키워드가 있는 주문 목록 조회
     */
    Page<OrderHistResponseDto> searchOrders(OrderSearchCond cond, Pageable pageable);

    /**
     * 주문 목록 검색 (판매자 주문 관리 페이지에서 사용)
     *
     * @param seller : 상점 소유 회원
     * @param cond 조건
     * @return 검색한 키워드가 있는 주문 목록 조회
     */
    Page<OrderHistResponseDto> searchOrdersSeller(Member seller, OrderSearchCond cond, Pageable pageable);

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
     * 주문 상태 ORDER 인 주문 목록 삭제(전체)
     **/

    void deleteOrdersInBatches(OrderStatus status, int batchSize);

    /**
     * 주문 취소
     *
     * @param orderNo : 취소할 주문번호
     * @param orderStatus : 변경할 주문상태
     * @param member : 검증할 회원
     * @param returnMessage : 반품 사유 or 주문취소 사유
     */
    void cancelOrder(Long orderNo, OrderStatus orderStatus, Member member, String returnMessage);

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

    /**
     * 주문 삭제
     *
     * @param member : 일치하는 회원인지 확인
     * @param orderNo : 삭제할 주문 번호
     */
    void deleteMyOrder(Member member, Long orderNo);

    /**
     * 주문 상태 변경 (관리자 또는 판매자가 주문 관리에서 주문 상태 변경)
     *
     * @param orderNo : 주문 상태 변경하려는 주문
     * @param orderStatus : 변경하려는 주문 상태
     * @param returnMessage : 반품 사유 or 주문취소 사유
     */
    void changeOrderState(Long orderNo, OrderStatus orderStatus, String returnMessage);
}
