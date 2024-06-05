package com.market.domain.chat.service;

import com.market.domain.chat.dto.ChatListResponseDto;
import com.market.domain.chat.dto.ChatMessageDto;

public interface ChatService {

    /**
     * 채팅방 내 채팅 목록 조회
     *
     * @param roomId 조회할 채팅 방 ID
     * @return       조회된 메세지 목록
     */
    ChatListResponseDto getAllChatByRoomId(Long roomId);

    /**
     * 채팅 메세지 저장
     *
     * @param roomId     저장할 채팅 방 ID
     * @param requestDto 메세지 저장 요청정보
     */
    void saveMessage(Long roomId, ChatMessageDto requestDto);
}