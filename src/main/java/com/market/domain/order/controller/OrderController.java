package com.market.domain.order.controller;

import com.market.domain.order.dto.SaveDeliveryRequestDto;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.service.OrderService;
import com.market.domain.orderItem.dto.OrderItemHistResponseDto;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import com.market.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders") // 단건 주문
    public ResponseEntity<Long> createOrder(@RequestBody OrderItemRequestDto requestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orderNo = orderService.order(requestDto, userDetails.getMember());
        return ResponseEntity.ok().body(orderNo);
    }

    @PutMapping("/orders/delivery") // 주문 시 배송지 저장
    public ResponseEntity<String> saveDeliveryAddr(@AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody SaveDeliveryRequestDto saveDeliveryRequestDto) {
        orderService.setDeliveryAddr(userDetails.getMember(), saveDeliveryRequestDto);
        return ResponseEntity.ok().body(saveDeliveryRequestDto.getDeliveryAddr());
    }

    @GetMapping("/orders") // 주문 목록 조회
    public ResponseEntity<Page<OrderHistResponseDto>> getOrdersWithMember(
        @AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        return ResponseEntity.ok()
            .body(orderService.getOrderList(userDetails.getMember(), pageable));
    }

    @GetMapping("/orderitems") // (가장 최근) 주문 내 상품 목록 조회
    public ResponseEntity<List<OrderItemHistResponseDto>> getOrderItemList(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok().body(orderService.getOrderItemList(userDetails.getMember()));
    }

    @PostMapping("/orders/{orderNo}/cancel") // 주문 취소
    public ResponseEntity<Long> cancelOrder(@PathVariable("orderNo") Long orderNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        orderService.cancelOrder(orderNo, userDetails.getMember());
        return ResponseEntity.ok().body(orderNo);
    }
}