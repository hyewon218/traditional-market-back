package com.market.domain.chatRoom.dto;

import com.market.domain.chatRoom.entity.ChatRoom;
import com.market.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ChatRoomRequestDto {
    public ChatRoom toEntity(Member member, Member receiver) {
        return ChatRoom.builder()
            .title("1:1 채팅상담")
            .member(member)
            .receiver(receiver)
            .build();
    }
}