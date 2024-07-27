package com.market.domain.inquiryAnswer.controller;

import com.market.domain.inquiryAnswer.dto.InquiryAnswerRequestDto;
import com.market.domain.inquiryAnswer.dto.InquiryAnswerResponseDto;
import com.market.domain.inquiryAnswer.service.InquiryAnswerService;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/inquiryanswer")
public class InquiryAnswerController {

    private final InquiryAnswerService inquiryAnswerService;

    // 문의사항 답변 생성
    @PostMapping("/{inquiryNo}")
    public ResponseEntity<InquiryAnswerResponseDto> createAnswer(
            @ModelAttribute InquiryAnswerRequestDto requestDto,
            @RequestPart (value = "imageFiles", required = false) List<MultipartFile> files,
            @PathVariable Long inquiryNo) throws IOException {
        InquiryAnswerResponseDto savedInquiryAnswer = inquiryAnswerService.createAnswer(requestDto, inquiryNo, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedInquiryAnswer);
    }

    // 문의사항에 해당하는 답변 조회
    @GetMapping("/{inquiryNo}")
    public ResponseEntity<InquiryAnswerResponseDto> findAnswer(@AuthenticationPrincipal UserDetailsImpl userDetails,
                                                               @PathVariable Long inquiryNo) {
        InquiryAnswerResponseDto inquiryAnswer = inquiryAnswerService.getAnswer(userDetails.getMember(), inquiryNo);
        return ResponseEntity.ok().body(inquiryAnswer);
    }


}
