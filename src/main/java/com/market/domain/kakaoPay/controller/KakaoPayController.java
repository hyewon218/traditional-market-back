package com.market.domain.kakaoPay.controller;

import com.market.domain.kakaoPay.dto.cancel.CancelResponseDto;
import com.market.domain.kakaoPay.dto.payment.ApproveResponseDto;
import com.market.domain.kakaoPay.dto.payment.ReadyResponseDto;
import com.market.domain.kakaoPay.service.KakaoPayService;
import com.market.domain.order.constant.OrderStatus;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;

    @PostMapping("/ready") // 결제 요청
    public ResponseEntity<ReadyResponseDto> readyToKakaoPay(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok().body(kakaoPayService.kakaoPayReady(userDetails.getMember()));
    }

    @GetMapping("/success") // 결제 성공
    public ResponseEntity<ApproveResponseDto> afterKakaoPayRequest(
        HttpServletResponse response,
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestParam("pg_token") String pgToken) throws IOException {
        ApproveResponseDto approveResponseDto = kakaoPayService.kakaoPayApprove(pgToken,
            userDetails.getMember());
        // 결제 완료 후 리액트 결제 완료된 주문 상세 페이지로 이동
        response.sendRedirect("http://localhost:3000/order-complete");
        return ResponseEntity.ok()
            .body(approveResponseDto);
    }

    @GetMapping("/cancel") // 결제 진행 중 취소
    public ResponseEntity<?> cancelOrder() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
            .body(new ApiResponse("사용자가 결제를 취소했습니다.", HttpStatus.EXPECTATION_FAILED.value()));
    }

    @GetMapping("/fail") // 결제 실패
    public ResponseEntity<?> failOrder() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
            .body(new ApiResponse("결제 실패했습니다.", HttpStatus.EXPECTATION_FAILED.value()));
    }

    @PostMapping("/cancel/{orderNo}") // 결제 취소
    public ResponseEntity<CancelResponseDto> cancelOrder(@PathVariable Long orderNo,
        OrderStatus orderStatus, String returnMessage) {
        return ResponseEntity.ok().body(kakaoPayService.kakaoPayCancel(orderNo, orderStatus, returnMessage));
    }
}
