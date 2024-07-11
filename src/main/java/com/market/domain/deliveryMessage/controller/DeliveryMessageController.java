package com.market.domain.deliveryMessage.controller;

import com.market.domain.deliveryMessage.dto.DeliveryMessageRequestDto;
import com.market.domain.deliveryMessage.dto.DeliveryMessageResponseDto;
import com.market.domain.deliveryMessage.entity.DeliveryMessage;
import com.market.domain.deliveryMessage.repository.DeliveryMessageRepository;
import com.market.domain.deliveryMessage.service.DeliveryMessageServiceImpl;
import com.market.domain.member.entity.Member;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/deliverymessage")
public class DeliveryMessageController {

    private final DeliveryMessageServiceImpl deliveryMessageService;
    private final DeliveryMessageRepository deliveryMessageRepository;

    // 배송 메시지 추가
    @PostMapping("")
    public ResponseEntity<DeliveryMessage> createDeliveryMessage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                                 @RequestBody DeliveryMessageRequestDto deliveryMessageRequestDto) {
        Member member = userDetails.getMember();
        DeliveryMessage savedDeliveryMessage = deliveryMessageService.createDeliveryMessage(deliveryMessageRequestDto, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedDeliveryMessage);
    }

    // 배송 메시지 목록 조회
    @GetMapping("")
    public ResponseEntity<List<DeliveryMessageResponseDto>> findDeliveryMessages(
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        List<DeliveryMessageResponseDto> deliveryMessages = deliveryMessageService.findAll(userDetails.getMember().getMemberNo());
        return ResponseEntity.ok().body(deliveryMessages);
    }
    
    // 배송 메시지 삭제
    @DeleteMapping("{deliveryMessageNo}")
    public ResponseEntity<?> deleteDeliveryMessage(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                             @PathVariable long deliveryMessageNo) {
        Long memberNo = userDetails.getMember().getMemberNo();
        DeliveryMessage deliveryMessage = deliveryMessageRepository.findById(deliveryMessageNo)
                        .orElseThrow(() -> new IllegalArgumentException("해당하는 배송 메시지가 없습니다"));

        if (memberNo == deliveryMessage.getMemberNo()) {
            deliveryMessageService.delete(deliveryMessageNo);
            return ResponseEntity.ok().body(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("일치하는 회원이 아닙니다");
        }
    }
    
}
