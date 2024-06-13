package com.market.domain.notification.controller;

import com.market.domain.notification.dto.NotificationResponseDto;
import com.market.domain.notification.service.NotificationService;
import com.market.global.security.UserDetailsImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
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
    public ResponseEntity<Page<NotificationResponseDto>> alarm(Pageable pageable,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(notificationService.alarmList(
            userDetails.getMember().getMemberNo(), pageable));
    }
}