package com.market.domain.notification.controller;

import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.dto.NotificationResponseDto;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.global.response.ApiResponse;
import com.market.global.security.UserDetailsImpl;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "http://localhost:3000", allowCredentials = "true")
@RequestMapping("/api")
public class NotificationController {

    private final NotificationService notificationService;
    //private final NotificationProducer notificationProducer;

    @GetMapping(value = "/notifications/subscribe") // sse 알람 구독
    public SseEmitter subscribe(@AuthenticationPrincipal UserDetailsImpl userDetails) {
        log.info("subscribe");
        return notificationService.connectNotification(userDetails.getMember().getMemberNo());
    }

    @PostMapping(value = "/notifications/send") // 테스트용 폴링, 롱폴링 알람 생성
    public void sendPolling() {
        notificationService.sendPolling(NotificationType.NEW_CHAT_ON_CHATROOM,
            NotificationArgs.of(2L, 1L), 1L);
    }

    @PostMapping(value = "/notifications/send-sse") // 테스트용 sse(+kafka) 알람 생성
    public void sendSSE() {
        notificationService.send(NotificationType.NEW_CHAT_ON_CHATROOM,
            NotificationArgs.of(2L, 1L), 1L);
        //kafka
/*        notificationProducer.send(new NotificationEvent(NotificationType.NEW_CHAT_ON_CHATROOM,
            NotificationArgs.of(2L, 1L), 1L));*/
    }

    // 폴링
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value ="/notifications/poll")
    public ResponseEntity<Page<NotificationResponseDto>> pollNotifications(Pageable pageable,
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        // 새 알림이 없더라도 빈 목록을 즉시 반환
        // 전체 알람목록을 다 반환할 필요는 없다. -> 롱폴링처럼 안읽은알람으로?! 페이징도 필요 없?
        return ResponseEntity.ok(notificationService.notificationList(
            userDetails.getMember().getMemberNo(), pageable));
    }

    // 롱폴링
    @CrossOrigin(origins = "http://localhost:3000")
    @GetMapping(value = "/notifications/longpoll")
    public DeferredResult<ResponseEntity<List<NotificationResponseDto>>> longPollNotifications(
        @AuthenticationPrincipal UserDetailsImpl userDetails) {
        long timeout = 300000L; // 5 minutes timeout
        DeferredResult<ResponseEntity<List<NotificationResponseDto>>> output = new DeferredResult<>(timeout);

        notificationService.longPollNotifications(1L, output, timeout);

        output.onCompletion(() -> log.info("Request completed: " + output.getResult()));
        output.onTimeout(() ->  log.info("Request timed out without new notifications."));
        return output;
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