package com.market.domain.chat.dto;

import com.market.domain.chat.entity.Chat;
import com.market.domain.chatRoom.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Builder
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageDto {
    private Long roomId;
    private String sender;
    private String message;

    public Chat toEntity(ChatRoom chatRoom) {
        return Chat.builder()
            .chatRoom(chatRoom)
            .sender(this.sender)
            .message(this.message)
            .build();
    }
}