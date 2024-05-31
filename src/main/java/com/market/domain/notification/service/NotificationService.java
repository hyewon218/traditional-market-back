package com.market.domain.notification.service;

import com.market.domain.member.entity.Member;
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
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class NotificationService {

    private final static String ALARM_NAME = "alarm";

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    public SseEmitter connectNotification(Long memberNo) {
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        emitterRepository.save(memberNo, emitter);
        emitter.onCompletion(() -> emitterRepository.delete(memberNo));
        emitter.onTimeout(() -> emitterRepository.delete(memberNo));

        try {
            log.info("send");
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

    // 댓글, 좋아요, 상품 판매 시 SseEmitter 보내줌
    public void send(NotificationType type, NotificationArgs args, Member receiver) {
        Notification notification = Notification.toEntity(type, args, receiver);
        notificationRepository.save(notification); // save alarm
        // 알람을 받아 볼 member 가 브라우저에 접속을 한 상태여야,
        // 알람페이지를 한번 들어간 상태여야지 인스턴스를 만들어서 가지고 있다.
        emitterRepository.get(receiver.getMemberNo()).ifPresentOrElse(sseEmitter -> {
                try {
                    sseEmitter.send(SseEmitter.event() // 새로운 알람이 발생했다!
                        .id(notification.getNo().toString())
                        .name(ALARM_NAME)
                        .data(new NotificationEvent()));
                } catch (IOException exception) {
                    emitterRepository.delete(receiver.getMemberNo());
                    throw new BusinessException(ErrorCode.NOTIFICATION_CONNECT_ERROR);
                }
            },
            () -> log.info("No emitter founded")
        );
    }

    @Transactional
    public Page<NotificationResponseDto> alarmList(Long memberNo, Pageable pageable) {
        return notificationRepository.findAllByMember_MemberNo(memberNo, pageable)
            .map(NotificationResponseDto::of);
    }
}