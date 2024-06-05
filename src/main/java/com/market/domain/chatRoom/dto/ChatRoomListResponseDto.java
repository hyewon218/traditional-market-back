package com.market.domain.chatRoom.dto;

import com.market.domain.chatRoom.entity.ChatRoom;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatRoomListResponseDto {

    private List<ChatRoomResponseDto> chatRoomList;

    public static ChatRoomListResponseDto of(List<ChatRoom> chatRoomList) {

        return ChatRoomListResponseDto.builder()
            .chatRoomList(chatRoomList.stream()
                .map(ChatRoomResponseDto::of)
                .toList())
            .build();
    }
}