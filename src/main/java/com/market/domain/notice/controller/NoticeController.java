package com.market.domain.notice.controller;

import com.market.domain.notice.dto.NoticeRequestDto;
import com.market.domain.notice.dto.NoticeResponseDto;
import com.market.domain.notice.repository.NoticeSearchCond;
import com.market.domain.notice.service.NoticeServiceImpl;
import com.market.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeServiceImpl noticeService;

    // 공지사항 생성
    @PostMapping("")
    public ResponseEntity<NoticeResponseDto> createNotice(@ModelAttribute NoticeRequestDto noticeRequestDto,
                                                          @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
            throws IOException {
        NoticeResponseDto savedNotice = noticeService.createNotice(noticeRequestDto, files);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedNotice);
    }

    // 공지사항 전체 조회
    @GetMapping("")
    public ResponseEntity<Page<NoticeResponseDto>> findNotices(Pageable pageable) {
        Page<NoticeResponseDto> notices = noticeService.findAll(pageable);
        return ResponseEntity.ok().body(notices);
    }

    // 키워드 검색 공지사항 목록 조회
    @GetMapping("/search")
    public ResponseEntity<Page<NoticeResponseDto>> searchNotices(NoticeSearchCond cond,
        Pageable pageable) {
        Page<NoticeResponseDto> result = noticeService.searchNotices(cond, pageable);
        return ResponseEntity.ok().body(result);
    }

    // 특정 공지사항 조회
    @GetMapping("/{noticeNo}")
    public ResponseEntity<NoticeResponseDto> findNotice(@PathVariable long noticeNo, HttpServletRequest request) {
        NoticeResponseDto notice = noticeService.getNotice(noticeNo, request);
        return ResponseEntity.ok().body(notice);
    }

    // 공지사항 수정
    @PutMapping("/{noticeNo}")
    public ResponseEntity<NoticeResponseDto> updateNotice(@PathVariable long noticeNo,
                                               @ModelAttribute NoticeRequestDto requestDto,
                                               @RequestPart(value = "imageFiles", required = false) List<MultipartFile> files)
            throws IOException {
        NoticeResponseDto updatedNotice = noticeService.update(noticeNo, requestDto, files);
        return ResponseEntity.ok().body(updatedNotice);
    }
    
    // 공지사항 삭제
    @DeleteMapping("{noticeNo}")
    public ResponseEntity<ApiResponse> deleteNotice(@PathVariable long noticeNo) {
        noticeService.delete(noticeNo);
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

}
