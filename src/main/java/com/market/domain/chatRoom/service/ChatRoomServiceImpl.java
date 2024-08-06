package com.market.domain.chatRoom.service;

import com.market.domain.chatRoom.dto.ChatRoomListResponseDto;
import com.market.domain.chatRoom.dto.ChatRoomRequestDto;
import com.market.domain.chatRoom.dto.ChatRoomResponseDto;
import com.market.domain.chatRoom.entity.ChatRoom;
import com.market.domain.chatRoom.repository.ChatRoomRepository;
import com.market.domain.member.entity.Member;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;

    // 채팅방 생성
    @Override
    @Transactional
    public ChatRoomResponseDto createChatRoom(ChatRoomRequestDto requestDto, Member member) {
        ChatRoom chatRoom = requestDto.toEntity(member);
        chatRoomRepository.save(chatRoom);
        return ChatRoomResponseDto.of(chatRoom);
    }

    // 채팅방 목록 조회
    @Override
    @Transactional(readOnly = true)
    public ChatRoomListResponseDto getChatRooms() {
        List<ChatRoom> chatRoomList = chatRoomRepository.findAllByOrderByCreateTimeDesc();
        return ChatRoomListResponseDto.of(chatRoomList);
    }

    // 내 채팅방 목록 조회
    @Override
    @Transactional(readOnly = true)
    public ChatRoomListResponseDto getMyChatRooms(Member member) {
        List<ChatRoom> chatRoomList = chatRoomRepository.findAllByMember_MemberNoOrderByCreateTimeDesc(
            member.getMemberNo());

        return ChatRoomListResponseDto.of(chatRoomList);
    }

    // 채팅방 단건 조회
    @Override
    @Transactional(readOnly = true)
    public ChatRoomResponseDto getChatRoom(Long id) {
        ChatRoom chatRoom = findChatRoom(id);

        return ChatRoomResponseDto.of(chatRoom);
    }

    // 채팅방 삭제
    @Override
    @Transactional
    public void deleteChatRoom(Long id, Member member) {
        ChatRoom chatRoom = findChatRoom(id);

        if (!chatRoom.getMember().getMemberNo().equals(member.getMemberNo())) {
            throw new BusinessException(ErrorCode.ONLY_MASTER_DELETE);
        }
        chatRoomRepository.delete(chatRoom);
    }

    // 채팅방 찾기
    private ChatRoom findChatRoom(Long id) {
        return chatRoomRepository.findById(id).orElseThrow(() ->
            new BusinessException(ErrorCode.NOT_FOUND_CHATROOM));
    }
}