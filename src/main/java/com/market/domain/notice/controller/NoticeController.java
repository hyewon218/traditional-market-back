package com.market.domain.notice.controller;

import com.market.domain.notice.dto.NoticeRequestDto;
import com.market.domain.notice.dto.NoticeResponseDto;
import com.market.domain.notice.dto.NoticeUpdateRequestDto;
import com.market.domain.notice.entity.Notice;
import com.market.domain.notice.service.NoticeServiceImpl;
import com.market.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notices")
public class NoticeController {

    private final NoticeServiceImpl noticeService;

    // 공지사항 생성
    @PostMapping("")
    public ResponseEntity<Notice> createNotice(@RequestBody NoticeRequestDto noticeRequestDto) {
        Notice savedNotice = noticeService.createNotice(noticeRequestDto);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(savedNotice);
    }

    // 공지사항 전체 조회
    @GetMapping("")
    public ResponseEntity<List<NoticeResponseDto>> findNotices() {
        List<NoticeResponseDto> notices = noticeService.findAll();
        return ResponseEntity.ok().body(notices);
    }

    // 특정 공지사항 조회
    @GetMapping("/{noticeNo}")
    public ResponseEntity<NoticeResponseDto> findNotice(@PathVariable long noticeNo) {
        Notice notice = noticeService.findById(noticeNo);
        return ResponseEntity.ok().body(NoticeResponseDto.of(notice));
    }

    // 공지사항 수정
    @PutMapping("/{noticeNo}")
    public ResponseEntity<Notice> updateNotice(@PathVariable long noticeNo,
                                               @RequestBody NoticeUpdateRequestDto updateRequestDto) {
        Notice updatedNotice = noticeService.update(noticeNo, updateRequestDto);
        return ResponseEntity.ok().body(updatedNotice);
    }
    
    // 공지사항 삭제
    @DeleteMapping("{noticeNo}")
    public ResponseEntity<ApiResponse> deleteNotice(@PathVariable long noticeNo) {
        noticeService.delete(noticeNo);
        return ResponseEntity.ok(new ApiResponse("삭제 성공", HttpStatus.OK.value()));
    }

}
