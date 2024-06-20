package com.market.domain.chat.controller;

import com.market.domain.chat.dto.ChatMessageDto;
import com.market.domain.chat.service.ChatService;
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

    // stompConfig 에서 설정한 applicationDestinationPrefixes 와 @MessageMapping 경로가 병합됨 (/pub + ...)
    // /pub/chat/message 에 메세지가 오면 동작
    @MessageMapping("chat/message/{roomId}")
    @SendTo("/sub/chat/{roomId}") // react callback 함수 실행
    public ChatMessageDto message(@DestinationVariable Long roomId, ChatMessageDto messageDto) {
        chatService.saveMessage(roomId, messageDto);

        return ChatMessageDto.builder()
            .roomId(roomId)
            .sender(messageDto.getSender())
            .message(messageDto.getMessage())
            .createdAt(messageDto.getCreatedAt())
            .build();
    }
}