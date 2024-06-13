package com.market.domain.inquiry.controller;

import com.market.domain.inquiry.dto.InquiryRequestDto;
import com.market.domain.inquiry.dto.InquiryResponseDto;
import com.market.domain.inquiry.dto.InquiryUpdateRequestDto;
import com.market.domain.inquiry.entity.Inquiry;
import com.market.domain.inquiry.repository.InquiryRepository;
import com.market.domain.inquiry.service.InquiryServiceImpl;
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

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryServiceImpl inquiryService;
    private final InquiryRepository inquiryRepository;
    
    // 공지사항 생성
    @PostMapping("")
    public ResponseEntity<InquiryResponseDto> createInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @RequestBody InquiryRequestDto inquiryRequestDto) {
        Member member = userDetails.getMember();
        Inquiry savedInquiry = inquiryService.createInquiry(inquiryRequestDto, member);
        return ResponseEntity.status(HttpStatus.CREATED).body(InquiryResponseDto.of(savedInquiry));
    }

    // 전체 문의사항 조회(본인의 문의사항만 조회)
    @GetMapping("")
    public ResponseEntity<?> findInquiries(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String memberId = userDetails.getMember().getMemberId();
        List<InquiryResponseDto> inquiries = inquiryService.findAll(memberId);

        if (!inquiries.isEmpty()) {
            return ResponseEntity.ok().body(inquiries);
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("문의사항이 존재하지않습니다");
        }
    }

    // 특정 문의사항 조회
    @GetMapping("{inquiryNo}")
    public ResponseEntity<?> findInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable long inquiryNo) {
        String memberId = userDetails.getMember().getMemberId();
        Inquiry inquiry = inquiryService.findById(inquiryNo);

        if (memberId.equals(inquiry.getMember().getMemberId())) {
            return ResponseEntity.ok().body(InquiryResponseDto.of(inquiry));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디가 일치하지않습니다");
        }
    }

    // 문의사항 수정
    @PutMapping("{inquiryNo}")
    public ResponseEntity<?> updateInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable long inquiryNo,
                                           @RequestBody InquiryUpdateRequestDto updateRequestDto) {
        String memberId = userDetails.getMember().getMemberId();
        Inquiry inquiry = inquiryService.findById(inquiryNo);

        if (memberId.equals(inquiry.getMember().getMemberId())) {
            Inquiry updatedInquiry = inquiryService.update(inquiryNo, updateRequestDto);
            return ResponseEntity.ok().body(InquiryResponseDto.of(updatedInquiry));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디가 일치하지않습니다");
        }
    }

    // 문의사항 개별 삭제
    @DeleteMapping("{inquiryNo}")
    public ResponseEntity<?> deleteInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable long inquiryNo) {
        String memberId = userDetails.getMember().getMemberId();
        Inquiry inquiry = inquiryService.findById(inquiryNo);

        if (memberId.equals(inquiry.getMember().getMemberId())) {
            inquiryService.delete(inquiryNo);
            return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디가 일치하지않습니다");
        }
    }

    // 문의사항 전체 삭제
    @DeleteMapping("")
    public ResponseEntity<?> deleteAllInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        String memberId = userDetails.getMember().getMemberId();
        inquiryService.deleteAll(memberId);
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

}
