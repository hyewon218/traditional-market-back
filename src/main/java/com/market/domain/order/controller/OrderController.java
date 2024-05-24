package com.market.domain.order.controller;

import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.service.OrderService;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class OrderController {

    private final OrderService orderService;

    @PostMapping("/orders") // 주문
    public ResponseEntity<Long> createOrder(@RequestBody OrderItemRequestDto requestDto,
        @AuthenticationPrincipal UserDetailsImpl userDetails
    ) {
        Long orderNo = orderService.order(requestDto, userDetails.getMember());
        return ResponseEntity.ok().body(orderNo);
    }

    @GetMapping("/orders") // 주문 조회
    public ResponseEntity<Page<OrderHistResponseDto>> getOrdersWithMember(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam("page") int page,
        @RequestParam("size") int size,
        @RequestParam("sortBy") String sortBy,
        @RequestParam("isAsc") boolean isAsc) {
        Page<OrderHistResponseDto> result = orderService.getOrderList(userDetails.getMember(),
            page, size, sortBy, isAsc);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/orders/{orderNo}/cancel") // 주문 취소
    public ResponseEntity<Long> cancelOrder(@PathVariable("orderNo") Long orderNo,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        orderService.cancelOrder(orderNo, userDetails.getMember());
        return ResponseEntity.ok().body(orderNo);
    }
}