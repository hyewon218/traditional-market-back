package com.market.domain.order.dto;

import com.market.domain.order.entity.Order;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.orderItem.dto.OrderItemHistResponseDto;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Builder
public class OrderHistResponseDto {

    private Long orderNo; // 주문 아이디

    private String orderDate; // 주문 날짜

    private String finishDate; // 배송 완료 날짜

    private String purchaseCompleteDate; // 구매 확정 날짜
    
    private String orderCancelDate; // 주문 취소 날짜
    
    private String returnDate; // 반품 신청 날짜
    
    private String returnCompleteDate; // 반품 완료 날짜

    private String returnMessage; // 반품 사유 또는 주문취소 사유

    private String deliveryAddr; // 주문 배송지
    
    private String deliveryMessage; // 배송 메시지

    private String receiver; // 받는 사람

    private String phone; // 휴대전화번호

    private OrderStatus orderStatus; // 주문 상태

    private String orderStatusDisplayName; // 주문 상태의 표시 이름
    
    private String randomOrderNo; // 랜덤으로 생성되는 주문 번호
    
    private String paymentMethod; // 결제 수단

    private String memberId; // 주문 관리 페이지에서 테이블에 매핑할 필드

    private List<OrderItemHistResponseDto> orderItemList; // 주문상품 리스트

    public static OrderHistResponseDto of(Order order) {
        return OrderHistResponseDto.builder()
            .orderNo(order.getNo())
            .orderDate(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .finishDate(order.getFinishDate() != null ?
                order.getFinishDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
            .purchaseCompleteDate(order.getPurchaseCompleteDate() != null ?
                order.getPurchaseCompleteDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
            .orderCancelDate(order.getOrderCancelDate() != null ?
                order.getOrderCancelDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
            .returnDate(order.getReturnDate() != null ?
                order.getReturnDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
            .returnCompleteDate(order.getReturnCompleteDate() != null ?
                order.getReturnCompleteDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")) : null)
            .returnMessage(order.getReturnMessage())
            .deliveryAddr(order.getDeliveryAddr())
            .deliveryMessage(order.getDeliveryMessage())
            .receiver(order.getReceiver())
            .phone(order.getPhone())
            .orderStatus(order.getOrderStatus())
            .orderStatusDisplayName(order.getOrderStatus().toString())
            .orderItemList(
                order.getOrderItemList().stream().map(OrderItemHistResponseDto::of).toList())
            .randomOrderNo(order.getRandomOrderNo())
            .paymentMethod(order.getPaymentMethod())
            .memberId(order.getMember().getMemberId())
            .build();
    }
}
