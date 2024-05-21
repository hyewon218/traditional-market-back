package com.market.domain.order.dto;

import com.market.domain.order.entity.Order;
import com.market.domain.orderItem.constant.OrderStatus;
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

    private OrderStatus orderStatus; //주문 상태

    private List<OrderItemHistResponseDto> orderItemList; // 주문상품 리스트

    public static OrderHistResponseDto of(Order order) {
        return OrderHistResponseDto.builder()
            .orderNo(order.getNo())
            .orderDate(order.getOrderDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm")))
            .orderStatus(order.getOrderStatus())
            .orderItemList(
                order.getOrderItemList().stream().map(OrderItemHistResponseDto::of).toList())
            .build();
    }
}