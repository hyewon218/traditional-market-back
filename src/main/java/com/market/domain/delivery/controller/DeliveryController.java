package com.market.domain.delivery.controller;

import com.market.domain.delivery.dto.DeliveryRequestDto;
import com.market.domain.delivery.dto.DeliveryResponseDto;
import com.market.domain.delivery.dto.DeliveryUpdateRequestDto;
import com.market.domain.delivery.entity.Delivery;
import com.market.domain.delivery.repository.DeliveryRepository;
import com.market.domain.delivery.service.DeliveryServiceImpl;
import com.market.domain.member.entity.Member;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliveries")
public class DeliveryController {

    private final DeliveryServiceImpl deliveryService;
    private final DeliveryRepository deliveryRepository;

    // 배송지 추가
    @PostMapping("")
    public ResponseEntity<Delivery> createDelivery(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                   @RequestBody DeliveryRequestDto deliveryRequestDto) {
        Member member = userDetails.getMember();
        Delivery savedDelivery = deliveryService.createDelivery(deliveryRequestDto, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDelivery);
    }

    // 배송지 목록 조회
    @GetMapping("")
    public ResponseEntity<List<DeliveryResponseDto>> findDeliveries(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DeliveryResponseDto> deliveries = deliveryService.findAll(userDetails.getMember().getMemberNo());
        return ResponseEntity.ok().body(deliveries);
    }

    // 특정 배송지 조회
    @GetMapping("{deliveryNo}")
    public ResponseEntity<?> findDelivery(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                          @PathVariable long deliveryNo) {
        Long memberNo = userDetails.getMember().getMemberNo();
        Delivery delivery = deliveryService.findById(deliveryNo);

        if (memberNo == delivery.getMemberNo()) {
            return ResponseEntity.ok().body(DeliveryResponseDto.of(delivery));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("일치하는 회원이 없습니다");
        }
    }

    // 배송지 수정
    @PutMapping("/{deliveryNo}")
    public ResponseEntity<?> updateDelivery(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable long deliveryNo,
                                            @RequestBody DeliveryUpdateRequestDto deliveryUpdateRequestDto) {
        Long memberNo = userDetails.getMember().getMemberNo();
        Delivery delivery = deliveryRepository.findById(deliveryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 없습니다"));

        if (memberNo == delivery.getMemberNo()) {
            Delivery updateDelivery = deliveryService.update(deliveryNo, deliveryUpdateRequestDto);
            return ResponseEntity.ok().body(updateDelivery);
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("일치하는 회원이 아닙니다");
        }
    }

    // 배송지 삭제
    @DeleteMapping("{deliveryNo}")
    public ResponseEntity<?> deleteDelivery(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                            @PathVariable long deliveryNo) {
        Long memberNo = userDetails.getMember().getMemberNo();
        Delivery delivery = deliveryRepository.findById(deliveryNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 배송지가 없습니다"));

        if (memberNo == delivery.getMemberNo()) {
            deliveryService.delete(deliveryNo);
            return ResponseEntity.ok().body(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("일치하는 회원이 아닙니다");
        }

    }
}
