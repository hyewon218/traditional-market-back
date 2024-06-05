package com.market.domain.chatRoom.service;

import com.market.domain.chatRoom.dto.ChatRoomListResponseDto;
import com.market.domain.chatRoom.dto.ChatRoomRequestDto;
import com.market.domain.chatRoom.dto.ChatRoomResponseDto;
import com.market.domain.member.entity.Member;
import java.io.IOException;

public interface ChatRoomService {

    /**
     * 채팅방 생성
     *
     * @param requestDto 채팅방 저장 요청정보
     * @param member     채팅방 생성 요청자
     */
    void createChatRoom(ChatRoomRequestDto requestDto, Member member) throws IOException;

    /**
     * 채팅방 목록 조회
     *
     * @return 조회된 채팅방 목록
     */
    ChatRoomListResponseDto getChatRooms();

    /**
     * 내 채팅방 목록 조회
     *
     * @return 조회된 채팅방 목록
     */
    ChatRoomListResponseDto getMyChatRooms(Member member);

    /**
     * 채팅방 조회
     *
     * @param id 조회할 채팅방 id
     * @return 조회된 채팅방
     */
    ChatRoomResponseDto getChatRoom(Long id);

    /**
     * 채팅방 삭제
     *
     * @param id     삭제할 채팅방 id
     * @param member 채팅방 삭제 요청자
     */
    // 채팅방 삭제
    void deleteChatRoom(Long id, Member member);

}