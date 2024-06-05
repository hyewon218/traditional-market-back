package com.market.domain.chat.dto;

import com.market.domain.chat.entity.Chat;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatResponseDto {

    private Long roomId;

    private String sender;

    private String message;

    private LocalDateTime createdAt;

    public static ChatResponseDto of(Chat chat) {
        return ChatResponseDto.builder()
            .roomId(chat.getChatRoom().getNo())
            .sender(chat.getSender())
            .message(chat.getMessage())
            .createdAt(chat.getCreateTime())
            .build();
    }
}