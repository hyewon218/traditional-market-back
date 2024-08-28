package com.market.domain.notification.controller;

import com.market.domain.notification.dto.NotificationResponseDto;
import com.market.domain.notification.service.NotificationService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping(value = "/notifications/subscribe") // 알람 구독
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("subscribe");
        return notificationService.connectNotification(userDetails.getMember().getMemberNo());
    }

    @GetMapping("/notifications") // 알람 목록
    public ResponseEntity<Page<NotificationResponseDto>> getNotificationList(Pageable pageable,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(notificationService.notificationList(
            userDetails.getMember().getMemberNo(), pageable));
    }

    @GetMapping("/notifications/count") // 로그인한 사용자 알람 수 조회
    public ResponseEntity<Long> getUserNotificationCount(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(
            notificationService.countNotifications(userDetails.getMember().getMemberNo()));
    }

    @PutMapping("/notifications/{notificationNo}/read") // 알람 읽음 상태로 변경
    public ResponseEntity<ApiResponse> markNotificationAsRead(@PathVariable Long notificationNo) {
        notificationService.markNotificationAsRead(notificationNo);
        return ResponseEntity.ok()
            .body(new ApiResponse("알람 읽음 상태로 변경 성공", HttpStatus.OK.value()));
    }

    @GetMapping("/notifications/{notificationNo}/read")  // 알람 읽음 여부 조회
    public ResponseEntity<NotificationResponseDto> getNotificationIsRead(
        @PathVariable Long notificationNo) {
        return ResponseEntity.ok().body(notificationService.getNotificationIsRead(notificationNo));
    }

    @PutMapping("/notifications/{notificationNo}/unread") // 알람 읽지 않은 상태로 변경
    public ResponseEntity<ApiResponse> markNotificationAsUnRead(@PathVariable Long notificationNo) {
        notificationService.markNotificationAsUnRead(notificationNo);
        return ResponseEntity.ok()
            .body(new ApiResponse("알람 읽지 않음 상태로 변경 성공", HttpStatus.OK.value()));
    }
}