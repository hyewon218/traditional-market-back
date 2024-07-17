package com.market.domain.delivery.controller;

import com.market.domain.delivery.dto.DeliveryRequestDto;
import com.market.domain.delivery.dto.DeliveryResponseDto;
import com.market.domain.delivery.dto.DeliveryUpdateRequestDto;
import com.market.domain.delivery.service.DeliveryService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryService deliveryService;

    @PostMapping("")
    public ResponseEntity<DeliveryResponseDto> createDelivery( // 배송지 추가
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody DeliveryRequestDto deliveryRequestDto) {
        DeliveryResponseDto result = deliveryService.createDelivery(deliveryRequestDto,
            userDetails.getMember());
        return ResponseEntity.status(HttpStatus.CREATED).body(result);
    }

    @GetMapping("")
    public ResponseEntity<List<DeliveryResponseDto>> getDeliveries( // 배송지 목록 조회
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DeliveryResponseDto> deliveries = deliveryService.getAllDeliveries(
            userDetails.getMember());
        return ResponseEntity.ok().body(deliveries);
    }

    @GetMapping("{deliveryNo}")
    public ResponseEntity<DeliveryResponseDto> getDelivery( // 특정 배송지 조회
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long deliveryNo) {
        DeliveryResponseDto result = deliveryService.getDeliveryById(userDetails.getMember(),
            deliveryNo);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("/{deliveryNo}")
    public ResponseEntity<DeliveryResponseDto> updateDelivery( // 배송지 수정
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long deliveryNo,
        @RequestBody DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        DeliveryResponseDto result = deliveryService.updateDelivery(userDetails.getMember(),
            deliveryNo,
            deliveryUpdateRequestDto);
        return ResponseEntity.ok().body(result);
    }

    @DeleteMapping("{deliveryNo}")
    public ResponseEntity<ApiResponse> deleteDelivery( // 배송지 삭제
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long deliveryNo) {
        deliveryService.deleteDelivery(userDetails.getMember(), deliveryNo);
        return ResponseEntity.ok().body(new ApiResponse("배송지 삭제 성공", HttpStatus.OK.value()));
    }

    @PutMapping("/primary/{deliveryNo}")
    public ResponseEntity<ApiResponse> setPrimary( // 기본배송지 설정
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long deliveryNo) {
        deliveryService.setPrimary(userDetails.getMember(), deliveryNo);
        return ResponseEntity.ok().body(new ApiResponse("기본 배송지 설정 성공", HttpStatus.OK.value()));
    }

    @PutMapping("/delprimary") // 기본배송지 해제
    public ResponseEntity<ApiResponse> delPrimary(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        deliveryService.removePrimary(userDetails.getMember());
        return ResponseEntity.ok().body(new ApiResponse("기본 배송지 해제 성공", HttpStatus.OK.value()));
    }
}
