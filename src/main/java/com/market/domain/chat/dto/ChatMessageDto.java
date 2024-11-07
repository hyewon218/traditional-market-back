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

    private String type; // websocket 에서만 사용
    private Long roomId;
    private String sender;
    private String message;
    private String createdAt;

    public Chat toEntity(ChatRoom chatRoom) { // stomp 에서 사용
        return Chat.builder()
            .chatRoom(chatRoom)
            .sender(this.sender)
            .message(this.message)
            .createdAt(this.createdAt)
            .build();
    }

    public Chat toEntityWebSocket(ChatRoom chatRoom) { // websocket 에서 사용
        return Chat.builder()
            .chatRoom(chatRoom)
            .type(this.type)
            .sender(this.sender)
            .message(this.message)
            .createdAt(this.createdAt)
            .build();
    }
}