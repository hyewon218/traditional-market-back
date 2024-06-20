package com.market.domain.chat.dto;

import com.market.domain.chat.entity.Chat;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatResponseDto {

    private Long roomId;
    private String sender;
    private String message;
    private String createdAt;

    public static ChatResponseDto of(Chat chat) {
        return ChatResponseDto.builder()
            .roomId(chat.getChatRoom().getNo())
            .sender(chat.getSender())
            .message(chat.getMessage())
            .createdAt(chat.getCreatedAt())
            .build();
    }
}