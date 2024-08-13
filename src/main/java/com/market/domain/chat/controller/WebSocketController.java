package com.market.domain.chat.controller;

import com.market.domain.chat.dto.ChatMessageDto;
import com.market.domain.chat.service.ChatService;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberService;
import com.market.domain.notification.constant.NotificationType;
import com.market.domain.notification.entity.NotificationArgs;
import com.market.domain.notification.service.NotificationService;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
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
    private final NotificationService notificationService;

    // stompConfig 에서 설정한 applicationDestinationPrefixes 와 @MessageMapping 경로가 병합됨 (/pub + ...)
    // /pub/chat/message 에 메세지가 오면 동작
    @MessageMapping("chat/message/{roomId}")
    @SendTo("/sub/chat/{roomId}") // react callback 함수 실행
    public ChatMessageDto message(@DestinationVariable Long roomId, ChatMessageDto messageDto) {
        chatService.saveMessage(roomId, messageDto);
        // receiver 에게 알람 보내기
        Member sender = memberService.findByMemberId(messageDto.getSender()); // 로그인한 사용자
        Member receiver = memberService.findChatRoomRecipient(roomId, sender);

        if (receiver != null) {
            NotificationArgs notificationArgs = NotificationArgs.builder()
                .fromMemberNo(sender.getMemberNo())
                .targetId(roomId)
                .build();
            NotificationType notificationType = getNotificationType(receiver);

            notificationService.send(notificationType, notificationArgs, receiver);
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