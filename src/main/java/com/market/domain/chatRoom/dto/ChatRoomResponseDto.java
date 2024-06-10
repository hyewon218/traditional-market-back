package com.market.domain.chatRoom.dto;

import com.market.domain.chatRoom.entity.ChatRoom;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomResponseDto {

    private Long no;

    private String title;

    private String username;

    public static ChatRoomResponseDto of(ChatRoom chatRoom) {
        return ChatRoomResponseDto.builder()
            .no(chatRoom.getNo())
            .title(chatRoom.getTitle())
            .username(chatRoom.getMember().getMemberId())
            .build();
    }
}