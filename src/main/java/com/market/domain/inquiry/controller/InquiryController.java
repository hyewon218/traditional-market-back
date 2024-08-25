package com.market.domain.inquiry.controller;

import com.market.domain.inquiry.dto.InquiryRequestDto;
import com.market.domain.inquiry.dto.InquiryResponseDto;
import com.market.domain.inquiry.dto.InquiryUpdateRequestDto;
import com.market.domain.inquiry.repository.InquirySearchCond;
import com.market.domain.inquiry.service.InquiryServiceImpl;
import com.market.domain.member.entity.Member;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import java.io.IOException;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryServiceImpl inquiryService;

    @PostMapping("") // 문의사항 생성 (일일 5개까지만 생성 가능)
    public ResponseEntity<InquiryResponseDto> createInquiry(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @ModelAttribute InquiryRequestDto inquiryRequestDto,
        @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
        throws IOException {
        Member member = userDetails.getMember();
        InquiryResponseDto savedInquiry = inquiryService.createInquiry(inquiryRequestDto, member,
            files);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInquiry);
    }

    @GetMapping("{inquiryNo}") // 특정 문의사항 조회(admin 권한도 조회 가능)
    public ResponseEntity<InquiryResponseDto> findInquiry(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long inquiryNo) {
        return ResponseEntity.ok(inquiryService.getInquiry(userDetails.getMember(), inquiryNo));
    }

    @GetMapping("/m") // 전체 문의사항 조회(본인의 문의사항만 조회)
    public ResponseEntity<?> getMyInquiries(@AuthenticationPrincipal UserDetailsImpl userDetails,
        Pageable pageable) {
        Long memberNo = userDetails.getMember().getMemberNo();
        Page<InquiryResponseDto> inquiries = inquiryService.getAllMine(memberNo, pageable);
        return ResponseEntity.ok().body(inquiries);
    }

    @GetMapping("") // 전체 문의사항 조회(모두, 관리자만 가능)
    public ResponseEntity<?> getAllInquiries(Pageable pageable) {
        Page<InquiryResponseDto> inquiries = inquiryService.getAllInquiries(pageable);
        return ResponseEntity.ok().body(inquiries);
    }

    @GetMapping("/search") // 키워드 검색 문의사항 목록 조회
    public ResponseEntity<Page<InquiryResponseDto>> searchMarkets(InquirySearchCond cond,
        Pageable pageable) {
        Page<InquiryResponseDto> result = inquiryService.searchInquiries(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    @PutMapping("{inquiryNo}") // 문의사항 수정
    public ResponseEntity<InquiryResponseDto> updateInquiry(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @RequestBody InquiryUpdateRequestDto updateRequestDto,
        @PathVariable Long inquiryNo) {
        return ResponseEntity.ok().body(
            inquiryService.updateInquiry(userDetails.getMember().getMemberNo(), updateRequestDto,
                inquiryNo));
    }

    @DeleteMapping("{inquiryNo}") // 문의사항 개별 삭제(admin 권한도 가능)
    public ResponseEntity<ApiResponse> deleteInquiry(
        @AuthenticationPrincipal UserDetailsImpl userDetails,
        @PathVariable Long inquiryNo) {
        inquiryService.deleteInquiry(userDetails.getMember(), inquiryNo);
        return ResponseEntity.ok().body(new ApiResponse("문의사항 삭제 완료!", HttpStatus.OK.value()));
    }

    @DeleteMapping("/m")  // 문의사항 전체 삭제(본인것만)
    public ResponseEntity<ApiResponse> deleteAllInquiry(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        inquiryService.deleteAllMine(userDetails.getMember().getMemberNo());
        return ResponseEntity.ok(new ApiResponse("문의사항 삭제 성공", HttpStatus.OK.value()));
    }

    @DeleteMapping("") // 문의사항 전체 삭제(admin 만 가능)
    public ResponseEntity<ApiResponse> deleteAll(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        inquiryService.deleteAll(userDetails.getMember());
        return ResponseEntity.ok(new ApiResponse("문의사항 전체 삭제 성공", HttpStatus.OK.value()));
    }

    @GetMapping("/count") // 전체 문의사항 개수 조회
    public ResponseEntity<Long> countAll() {
        return ResponseEntity.ok().body(inquiryService.countInquiry());
    }
}
