package com.market.domain.chatRoom.dto;

import com.market.domain.chatRoom.entity.ChatRoom;
import com.market.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class ChatRoomRequestDto {

    public ChatRoom toEntity(Member member) {
        return ChatRoom.builder()
            .title("1:1 채팅상담")
            .member(member)
            .build();
    }
}