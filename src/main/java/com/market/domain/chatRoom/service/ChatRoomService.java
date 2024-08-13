package com.market.domain.chatRoom.service;

import com.market.domain.chatRoom.dto.ChatRoomRequestDto;
import com.market.domain.chatRoom.dto.ChatRoomResponseDto;
import com.market.domain.chatRoom.entity.ChatRoom;
import com.market.domain.member.entity.Member;
import java.io.IOException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ChatRoomService {

    /**
     * 채팅방 생성
     *
     * @param requestDto 채팅방 저장 요청정보
     * @param member     로그인한 사용자
     */
    ChatRoomResponseDto createChatRoom(ChatRoomRequestDto requestDto, Member member)
        throws IOException;

    /**
     * 채팅방 목록 조회
     *
     * @return 조회된 채팅방 목록
     */
    Page<ChatRoomResponseDto> getChatRooms(Pageable pageable);

    /**
     * 내 채팅방 목록 조회
     *
     * @return 조회된 채팅방 목록
     */
    Page<ChatRoomResponseDto> getMyChatRooms(Member member, Pageable pageable);

    /**
     * 채팅방 조회
     *
     * @param chatRoomNo 채팅방 no
     * @return 조회된 채팅방
     */
    ChatRoomResponseDto getChatRoom(Long chatRoomNo);

    /**
     * 채팅방 삭제
     *
     * @param chatRoomNo     삭제할 채팅방 no
     * @param member     로그인한 사용자
     */
    void deleteChatRoom(Long chatRoomNo, Member member);

    /**
     * 채팅방 찾기
     *
     * @param chatRoomNo 찾을 채팅방 no
     */
    ChatRoom findChatRoom(Long chatRoomNo);

    /**
     * 채팅방 읽음 상태 조회
     *
     * @param chatRoomNo 찾을 채팅방 no
     */
    ChatRoomResponseDto getChatRoomIsRead(Long chatRoomNo);

    /**
     * 채팅방 읽은 상태로 변경
     *
     * @param chatRoomNo 찾을 채팅방 no
     * @param member     로그인한 사용자
     */
    void markChatRoomAsRead(Long chatRoomNo, Member member);

    /**
     * 채팅방 읽지 않은 상태로 변경
     *
     * @param chatRoomNo 찾을 채팅방 no
     * @param member     로그인한 사용자
     */
    void markChatRoomAsUnRead(Long chatRoomNo, Member member);

    /**
     * 채팅방 작성자인지 관리자인지 확인
     *
     * @param chatRoomNo 찾을 채팅방 no
     * @param member     로그인한 사용자
     */
    void validateIsMasterAndAdmin(Long chatRoomNo, Member member);

    /**
     *  관리자인지 확인
     *
     * @param chatRoomNo 찾을 채팅방 no
     * @param member     로그인한 사용자
     */
    void validateIsAdmin(Long chatRoomNo, Member member);
}