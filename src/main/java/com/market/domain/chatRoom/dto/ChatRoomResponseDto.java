package com.market.domain.chatRoom.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.market.domain.chatRoom.entity.ChatRoom;
import java.time.LocalDateTime;
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

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "Asia/Seoul")
    private LocalDateTime createTime;

    public static ChatRoomResponseDto of(ChatRoom chatRoom) {
        return ChatRoomResponseDto.builder()
            .no(chatRoom.getNo())
            .title(chatRoom.getTitle())
            .username(chatRoom.getMember().getMemberId())
            .createTime(chatRoom.getCreateTime())
            .build();
    }
}