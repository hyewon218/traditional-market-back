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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/api/inquiries")
public class InquiryController {

    private final InquiryServiceImpl inquiryService;
    private final InquiryRepository inquiryRepository;
    
    // 문의사항 생성
    @PostMapping("")
    public ResponseEntity<InquiryResponseDto> createInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                            @ModelAttribute InquiryRequestDto inquiryRequestDto,
                                                            @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
            throws IOException {
        Member member = userDetails.getMember();
        InquiryResponseDto savedInquiry = inquiryService.createInquiry(inquiryRequestDto, member, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInquiry);
    }

    // 전체 문의사항 조회(본인의 문의사항만 조회)
    @GetMapping("/m")
    public ResponseEntity<?> getMyInquiries(@AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        Long memberNo = userDetails.getMember().getMemberNo();
        Page<InquiryResponseDto> inquiries = inquiryService.findAllMine(memberNo, pageable);
        return ResponseEntity.ok().body(inquiries);
    }

    // 전체 문의사항 조회(모두, 관리자만 가능)
    @GetMapping("")
    public ResponseEntity<?> getAllInquiries(Pageable pageable) {
        Page<InquiryResponseDto> inquiries = inquiryService.getAllInquiries(pageable);
        return ResponseEntity.ok().body(inquiries);
    }

    // 특정 문의사항 조회(admin 권한도 조회 가능)
    @GetMapping("{inquiryNo}")
    public ResponseEntity<?> findInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                         @PathVariable long inquiryNo) {
        Inquiry inquiry = inquiryService.findById(inquiryNo);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin || userDetails.getMember().getMemberNo().equals(inquiry.getMemberNo())) {
            inquiryService.findById(inquiryNo);
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("접근이 거부되었습니다");
        }
        return ResponseEntity.ok().body(InquiryResponseDto.of(inquiry));
    }

    // 문의사항 수정
    @PutMapping("{inquiryNo}")
    public ResponseEntity<?> updateInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable long inquiryNo,
                                           @RequestBody InquiryUpdateRequestDto updateRequestDto) {
        Long memberNo = userDetails.getMember().getMemberNo();
        Inquiry inquiry = inquiryService.findById(inquiryNo);

        if (memberNo == inquiry.getMemberNo()) {
            Inquiry updatedInquiry = inquiryService.update(inquiryNo, updateRequestDto);
            return ResponseEntity.ok().body(InquiryResponseDto.of(updatedInquiry));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디가 일치하지않습니다");
        }
    }

    // 문의사항 개별 삭제(admin 권한도 가능)
    @DeleteMapping("{inquiryNo}")
    public ResponseEntity<?> deleteInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                           @PathVariable long inquiryNo) {

        Inquiry inquiry = inquiryService.findById(inquiryNo);
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if (isAdmin || userDetails.getMember().getMemberNo().equals(inquiry.getMemberNo())) {
            inquiryService.delete(inquiryNo);
            return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
        } else {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("아이디가 일치하지않습니다");
        }
    }

    // 문의사항 전체 삭제(본인것만)
    @DeleteMapping("/m")
    public ResponseEntity<?> deleteAllInquiry(@AuthenticationPrincipal UserDetailsImpl userDetails, Pageable pageable) {
        Long memberNo = userDetails.getMember().getMemberNo();
        inquiryService.deleteAllMine(memberNo, pageable);
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

    // 문의사항 전체 삭제(admin만 가능)
    @DeleteMapping("")
    public ResponseEntity<?> deleteAll(Pageable pageable) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        boolean isAdmin = authentication.getAuthorities().stream().anyMatch(
                authority -> authority.getAuthority().equals("ROLE_ADMIN"));

        if(isAdmin) {
            inquiryService.deleteAll(pageable);
            return ResponseEntity.ok().body(new ApiResponse("전체 삭제 성공", HttpStatus.OK.value()));
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("권한이 없습니다");
        }
    }

    // 전체 문의사항 개수 조회
    @GetMapping("/count")
    public ResponseEntity<?> countAll() {
        Long count = inquiryService.countInquiry();
        return ResponseEntity.ok().body(count);
    }

}
