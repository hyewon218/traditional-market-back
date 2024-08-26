package com.market.domain.chatRoom.dto;

import com.market.domain.chatRoom.entity.ChatRoom;
import com.market.domain.member.entity.Member;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class ChatRoomRequestDto {
    private List<Member> receivers; // 여러 명의 receiver 를 위한 필드 추가

    public ChatRoom toEntity(Member member) {
        return ChatRoom.builder()
            .title("1:1 채팅상담")
            .member(member)
            .receivers(receivers)
            .build();
    }
}