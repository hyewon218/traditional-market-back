package com.market.domain.kakaoPay.controller;

import com.market.domain.kakaoPay.dto.cancel.CancelResponseDto;
import com.market.domain.kakaoPay.dto.payment.ApproveResponseDto;
import com.market.domain.kakaoPay.dto.payment.ReadyResponseDto;
import com.market.domain.kakaoPay.service.KakaoPayService;
import com.market.domain.member.entity.Member;
import com.market.domain.order.entity.Order;
import com.market.domain.order.repository.OrderRepository;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/payment")
public class KakaoPayController {

    private final KakaoPayService kakaoPayService;
    private final OrderRepository orderRepository;

    // 결제 요청
    @PostMapping("/ready")
    public ResponseEntity<ReadyResponseDto> readyToKakaoPay(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        Member member = userDetails.getMember();
        Order order = orderRepository.findFirstByMemberOrderByCreateTimeDesc(member)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원의 최근 주문을 찾을 수 없습니다"));

        return ResponseEntity.ok().body(kakaoPayService.kakaoPayReady(member, order));
    }

    // 결제 성공
    @GetMapping("/success")
    public ResponseEntity<ApproveResponseDto> afterKakaoPayRequest(
            @AuthenticationPrincipal UserDetailsImpl userDetails,
            @RequestParam("pg_token") String pgToken) {

        Member member = userDetails.getMember();
        Order order = orderRepository.findFirstByMemberOrderByCreateTimeDesc(member)
                .orElseThrow(() -> new IllegalArgumentException("해당 회원의 최근 주문을 찾을 수 없습니다"));

        ApproveResponseDto approveResponseDto = kakaoPayService.kakaoPayApprove(pgToken, member, order);
        return ResponseEntity.ok().body(approveResponseDto);
    }

    // 결제 진행 중 취소
    @GetMapping("/cancel")
    public ResponseEntity<?> cancelOrder() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body(new ApiResponse("사용자가 결제를 취소했습니다", HttpStatus.EXPECTATION_FAILED.value()));
    }

    // 결제 실패
    @GetMapping("/fail")
    public ResponseEntity<?> failOrder() {
        return ResponseEntity.status(HttpStatus.EXPECTATION_FAILED)
                .body(new ApiResponse("결제 실패했습니다", HttpStatus.EXPECTATION_FAILED.value()));
    }

    // 결제 취소
    @PostMapping("/cancel/{orderNo}")
    public ResponseEntity<CancelResponseDto> cancelOrder(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                         @PathVariable long orderNo) {
        Member member = userDetails.getMember();
        Order order = orderRepository.findById(orderNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 주문을 찾을 수 없습니다"));

        CancelResponseDto cancelResponseDto = kakaoPayService.kakaoPayCancel(member, order);
        return ResponseEntity.ok().body(cancelResponseDto);
    }
}
