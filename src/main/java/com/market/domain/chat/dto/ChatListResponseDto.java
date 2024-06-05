package com.market.domain.chat.dto;

import com.market.domain.chat.entity.Chat;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatListResponseDto {

    private List<ChatResponseDto> chatList;

    public static ChatListResponseDto of(List<Chat> chats) {

        return ChatListResponseDto.builder()
            .chatList(chats.stream()
                .map(ChatResponseDto::of)
                .toList())
            .build();
    }
}