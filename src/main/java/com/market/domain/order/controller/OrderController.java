package com.market.domain.order.controller;

import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.dto.SaveDeliveryRequestDto;
import com.market.domain.order.dto.OrderHistResponseDto;
import com.market.domain.order.repository.OrderSearchCond;
import com.market.domain.order.service.OrderService;
import com.market.domain.orderItem.dto.OrderItemHistResponseDto;
import com.market.domain.orderItem.dto.OrderItemRequestDto;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

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

    @GetMapping("/orderitems") // (가장 최근) 주문 내 상품 목록 조회(주문페이지)
    public ResponseEntity<List<OrderItemHistResponseDto>> getOrderItemList(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok().body(orderService.getOrderItemList(userDetails.getMember()));
    }

    @PutMapping("/orders/delivery") // 주문 시 배송지 저장
    public ResponseEntity<String> saveDeliveryAddr(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody SaveDeliveryRequestDto saveDeliveryRequestDto) {
        orderService.setDeliveryAddr(userDetails.getMember(), saveDeliveryRequestDto);
        return ResponseEntity.ok().body(saveDeliveryRequestDto.getDeliveryAddr() + ", " +
            saveDeliveryRequestDto.getDeliveryMessage() + ", " +
            saveDeliveryRequestDto.getReceiver() + ", " +
            saveDeliveryRequestDto.getPhone());
    }

    @GetMapping("/orders") // 가장 최근 주문 COMPLETE 주문 조회
    public ResponseEntity<OrderHistResponseDto> getOrder(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok()
            .body(orderService.getLatestOrder(userDetails.getMember()));
    }

    @GetMapping("/orders-page") // 본인의 CANCEL 제외한 모든 주문상태 주문 목록 조회
    public ResponseEntity<Page<OrderHistResponseDto>> getOrders(
        @AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        return ResponseEntity.ok()
            .body(orderService.getOrders(userDetails.getMember(), pageable));
    }

    @GetMapping("/orders-page/cancel") // 본인의 CANCEL 주문 목록 조회
    public ResponseEntity<Page<OrderHistResponseDto>> getCancelOrders(
        @AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        return ResponseEntity.ok()
            .body(orderService.getCancelOrders(userDetails.getMember(), pageable));
    }

    @GetMapping("/orders-page-admin") // CANCEL 제외한 모든 주문 목록 조회(관리자만 가능)
    public ResponseEntity<Page<OrderHistResponseDto>> getOrdersAdmin(Pageable pageable) {
        return ResponseEntity.ok()
            .body(orderService.getOrdersAdmin(pageable));
    }

    @GetMapping("/orders-page-admin/status") // 주문 상태별 목록 조회(관리자만 가능)
    public ResponseEntity<Page<OrderHistResponseDto>> getCancelOrdersAdmin(OrderStatus orderStatus,
        Pageable pageable) {
        return ResponseEntity.ok()
            .body(orderService.getOrderStatusAdmin(orderStatus, pageable));
    }

    @GetMapping("/orders-page-seller") // CANCEL 제외한 판매자가 자신이 소유한 상점의 상품들에 대한 주문 목록 조회 (판매자만 가능)
    public ResponseEntity<Page<OrderHistResponseDto>> getOrdersSeller(
        @AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        return ResponseEntity.ok()
            .body(orderService.getOrdersSeller(userDetails.getMember(), pageable));
    }

    @GetMapping("/orders-page-seller/cancel") // 판매자가 자신이 소유한 상점의 상품들에 대한 주문상태별 조회 (판매자만 가능)
    public ResponseEntity<Page<OrderHistResponseDto>> getCancelOrdersSeller(
        @AuthenticationPrincipal UserDetailsImpl userDetails, OrderStatus orderStatus, Pageable pageable) {
        return ResponseEntity.ok()
            .body(orderService.getCancelOrdersSeller(userDetails.getMember(), orderStatus, pageable));
    }

    @GetMapping("/orders/search") // 주문 목록 검색 (관리자 주문 관리 페이지에서 사용)
    public ResponseEntity<Page<OrderHistResponseDto>> searchOrders(OrderSearchCond cond,
        Pageable pageable) {
        Page<OrderHistResponseDto> result = orderService.searchOrders(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    @GetMapping("/orders/search/seller") // 주문 목록 검색 (판매자 주문 관리 페이지에서 사용)
    public ResponseEntity<Page<OrderHistResponseDto>> searchOrdersSeller(@AuthenticationPrincipal UserDetailsImpl userDetails, OrderSearchCond cond,
        Pageable pageable) {
        Page<OrderHistResponseDto> result = orderService.searchOrdersSeller(userDetails.getMember(), cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    @PostMapping("/orders/{orderNo}/cancel") // 주문 취소
    public ResponseEntity<Long> cancelOrder(@PathVariable("orderNo") Long orderNo, OrderStatus orderStatus,
        @AuthenticationPrincipal UserDetailsImpl userDetails, String returnMessage) {
        orderService.cancelOrder(orderNo, orderStatus, userDetails.getMember(), returnMessage);
        return ResponseEntity.ok().body(orderNo);
    }

    @DeleteMapping("/orders/{orderNo}") // 주문 삭제
    public ResponseEntity<ApiResponse> deleteMyOrder(
        @AuthenticationPrincipal UserDetailsImpl userDetails, @PathVariable Long orderNo) {
        orderService.deleteMyOrder(userDetails.getMember(), orderNo);
        return ResponseEntity.ok().body(new ApiResponse("주문 삭제 완료", HttpStatus.OK.value()));
    }

    @PutMapping("/orders/change/{orderNo}") // 주문 상태 변경 (관리자 또는 판매자가 주문 관리에서 주문 상태 변경)
    public ResponseEntity<ApiResponse> changeOrderStatus(@PathVariable Long orderNo,
        OrderStatus orderStatus, String returnMessage) {
        orderService.changeOrderState(orderNo, orderStatus, returnMessage);
        return ResponseEntity.ok().body(new ApiResponse("주문상태 변경 성공", HttpStatus.OK.value()));
    }

}