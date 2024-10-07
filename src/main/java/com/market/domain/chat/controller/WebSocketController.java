package com.market.domain.chat.controller;

import com.market.domain.chat.dto.ChatMessageDto;
import com.market.domain.chat.service.ChatService;
import com.market.domain.kafka.producer.NotificationProducer;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberService;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.entity.NotificationEvent;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;


@Log4j2
@Controller
@RequiredArgsConstructor
@RequestMapping("/api")
public class WebSocketController {

    private final ChatService chatService;
    private final MemberService memberService;
    private final NotificationProducer notificationProducer;

    // stompConfig 에서 설정한 applicationDestinationPrefixes 와 @MessageMapping 경로가 병합됨 (/pub + ...)
    // /pub/chat/message 에 메세지가 오면 동작
    @MessageMapping("chat/message/{roomId}")
    @SendTo("/sub/chat/{roomId}") // react callback 함수 실행
    public ChatMessageDto message(@DestinationVariable Long roomId, ChatMessageDto messageDto) {

        chatService.saveMessage(roomId, messageDto); // chat DB 저장
        /*receiver 에게 알람*/
        // sender: 로그인한 사용자
        Member sender = memberService.findByMemberId(messageDto.getSender());
        // receivers: 보낸 사람에 따라 받는 사람(들)을 결정
        List<Member> receivers = memberService.findChatRoomRecipients(roomId, sender);

        if (receivers != null && !receivers.isEmpty()) {
            NotificationArgs notificationArgs = NotificationArgs.of(sender.getMemberNo(), roomId);
            // 모든 수신자에게 알림 전송
            for (Member receiver : receivers) {
                NotificationType notificationType = getNotificationType(receiver);
                notificationProducer.send(new NotificationEvent(notificationType, notificationArgs,
                    receiver.getMemberNo()));
            }
        }
        return ChatMessageDto.builder()
            .roomId(roomId)
            .sender(messageDto.getSender())
            .message(messageDto.getMessage())
            .createdAt(messageDto.getCreatedAt())
            .build();
    }

    private static NotificationType getNotificationType(Member receiver) {
        NotificationType notificationType;
        if (receiver.getRole() == Role.ADMIN) {
            notificationType = NotificationType.NEW_CHAT_REQUEST_ON_CHATROOM; // 받는 사람이 관리자면 상담 요청 알람
        } else if (receiver.getRole() == Role.MEMBER) {
            notificationType = NotificationType.NEW_CHAT_ON_CHATROOM; // 받는 사람이 일반 사용자면 상담 답변 알람
        } else {
            throw new BusinessException(ErrorCode.INVALID_ROLE);
        }
        return notificationType;
    }
}