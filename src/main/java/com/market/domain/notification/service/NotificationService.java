package com.market.domain.notification.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.dto.NotificationResponseDto;
import com.market.domain.notification.entity.Notification;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.entity.NotificationEvent;
import com.market.domain.notification.repository.EmitterRepository;
import com.market.domain.notification.repository.NotificationRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.async.DeferredResult;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final static String ALARM_NAME = "alarm";

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter connectNotification(Long memberNo) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(memberNo, emitter);
        emitter.onCompletion(() -> emitterRepository.delete(memberNo));
        emitter.onTimeout(() -> emitterRepository.delete(memberNo));
        try {
            log.info("send"+emitter);
            // 클라이언트에게 연결 정보 제공
            emitter.send(SseEmitter.event()
                .id("id")
                .name(ALARM_NAME)
                .data("connect completed"));
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.NOTIFICATION_CONNECT_ERROR);
        }
        return emitter;
    }

    // 댓글, 좋아요, 상품 판매, 상담 요청 및 답변 시 SseEmitter 보내줌
    @Transactional
    public void send(NotificationType type, NotificationArgs args, Long receiverNo) {
        log.info("Receiver MemberNo: " + receiverNo);

        // 알림 생성 및 저장
        Notification notification = Notification.toEntity(type, args, receiverNo);
        notificationRepository.save(notification); // save alarm

        // 알람을 받아 볼 member 가 브라우저에 접속을 한 상태여야,
        // 알람페이지를 한번 들어간 상태여야지(subscribe) 인스턴스를 만들어서 가지고 있다.
        // 주어진 회원 번호에 대한 emitter(이벤트 발송기) 검색
        Optional<SseEmitter> emitterOptional = emitterRepository.get(receiverNo);

        // emitter(이벤트 발송기)의 존재 여부를 로그로 기록
        if (emitterOptional.isPresent()) {
            log.info("Emitter found for MemberNo: " + receiverNo);
        } else {
            log.info("No emitter found for MemberNo: " + receiverNo);
        }

        // 디버깅: emitter(이벤트 발송기)가 존재할 경우 해당 내용을 로그로 기록
        log.info("EmitterRepository.get(): " + emitterOptional.orElse(null));

        // emitter(이벤트 발송기)가 존재하는 경우 알림 전송을 진행
        emitterOptional.ifPresentOrElse(
            sseEmitter -> {
                try {
                    NotificationEvent notificationEvent = NotificationEvent.builder()
                        .type(notification.getNotificationType())
                        .args(notification.getArgs())
                        .receiverNo(notification.getReceiverNo())
                        .build();

                    // SSE 이벤트를 전송
                    sseEmitter.send(SseEmitter.event()
                        .id(notification.getNo().toString())
                        .name(ALARM_NAME)
                        .data(objectMapper.writeValueAsString(notificationEvent))); // JSON 형식으로 전송
                    log.info(
                        "Notification sent successfully to MemberNo: " + receiverNo);
                } catch (IOException exception) {
                    // 전송 실패 시 오류를 기록하고 처리
                    // IOException : SSE 데이터를 클라이언트에게 전송하는 과정에서 발생하는 오류
                    log.error("Failed to send notification. Removing emitter for MemberNo: "
                        + receiverNo, exception);
                    emitterRepository.delete(receiverNo);
                    throw new BusinessException(ErrorCode.NOTIFICATION_CONNECT_ERROR);
                }
            },
            () -> log.info("No emitter found for MemberNo: " + receiverNo)
        );
    }

    // 폴링, 롱폴링 (새 알람 생성만)
    @Transactional
    public void sendPolling(NotificationType type, NotificationArgs args, Long receiverNo) {
        log.info("sendPolling Receiver MemberNo: " + receiverNo);
        Notification notification = Notification.toEntity(type, args, receiverNo);
        notificationRepository.save(notification);
    }

    // 롱폴링
    public void longPollNotifications(Long memberNo,
        DeferredResult<ResponseEntity<List<NotificationResponseDto>>> output, long timeout) {
        ScheduledExecutorService executor = Executors.newScheduledThreadPool(1);
        long pollingInterval = 5; // Poll every 5 seconds

        // 서버 측에서 새 알림을 확인하기 위한 5초 간격을 설정
        ScheduledFuture<?> scheduledFuture = executor.scheduleWithFixedDelay(() -> {
            List<NotificationResponseDto> notifications = UnsentnotificationList(memberNo);
            if (!notifications.isEmpty()) {
                notifications.forEach(notification -> {
                    Notification notificationEntity = notificationRepository.findById(
                        notification.getNo()).orElse(null);
                    if (notificationEntity != null) {
                        notificationEntity.setIsSent(); // 알림을 보낸 것으로 표시
                        notificationRepository.save(notificationEntity); // 수정된 엔터티를 데이터베이스에 다시 저장
                    }
                });
                // 클라이언트에 응답을 반환. 그러면 실행 프로그램도 종료되어 추가 폴링이 중지된다.
                output.setResult(ResponseEntity.ok(notifications));
                log.info("output.setResult!??!??!?!!?"+output.getResult());
                executor.shutdown(); // 불필요한 리소스 사용 방지
            }
            // 5초마다 notificationList(memberNo) 메소드가 호출되어 사용자에게 새로운 알림이 있는지 확인
            // 알림이 발견되지 않으면 코드는 새 알림을 찾거나 시간 초과될 때까지 5초마다 계속 확인
        }, 0, pollingInterval, TimeUnit.SECONDS);

        output.onTimeout(() -> {
            log.info("Request timed out after " + timeout + " milliseconds.");
            output.setResult(ResponseEntity.ok(Collections.emptyList()));
            scheduledFuture.cancel(false);
            executor.shutdown();
        });
        output.onError(t -> {
            log.error("Error during long polling", t);
            executor.shutdown();
        });
    }

    // 룡폴링
    @Transactional(readOnly = true) // 클라이언트로 보내지지 않은 알람
    public List<NotificationResponseDto> UnsentnotificationList(Long memberNo) {
        return notificationRepository.findAllByReceiverNoAndIsSent(memberNo, false).stream()
            .map(NotificationResponseDto::of).toList();
    }

    @Transactional(readOnly = true) // 알람 목록 최신순 조회
    public Page<NotificationResponseDto> notificationList(Long memberNo, Pageable pageable) {
        return notificationRepository.findAllByReceiverNoOrderByCreateTimeDesc(memberNo, pageable)
            .map(NotificationResponseDto::of);
    }

    @Transactional(readOnly = true) // 로그인한 사용자 읽지 않은 알람 수 조회
    public Long countNotifications(Long memberNo) {
        return notificationRepository.countByReceiverNoAndIsRead(memberNo, false);
    }

    @Transactional // 알람 읽은 상태로 변경
    public void markNotificationAsRead(Long notificationNo) {
        Notification notification = findNotification(notificationNo);
        notification.markAsRead();
    }

    @Transactional(readOnly = true)  // 알람 읽음 상태 조회
    public NotificationResponseDto getNotificationIsRead(Long notificationNo) {
        Notification notification = findNotification(notificationNo);
        return NotificationResponseDto.of(notification);
    }

    @Transactional // 알람 읽지 않은 상태로 변경
    public void markNotificationAsUnRead(Long notificationNo) {
        Notification notification = findNotification(notificationNo);
        notification.markAsUnread();
    }

    @Transactional(readOnly = true) // 알람 찾기
    public Notification findNotification(Long notificationNo) {
        return notificationRepository.findById(notificationNo).orElseThrow(() ->
            new BusinessException(ErrorCode.NOT_FOUND_NOTIFICATION));
    }
}