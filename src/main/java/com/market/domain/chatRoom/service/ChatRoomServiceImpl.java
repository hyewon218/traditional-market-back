package com.market.domain.chatRoom.service;

import com.market.domain.chatRoom.dto.ChatRoomRequestDto;
import com.market.domain.chatRoom.dto.ChatRoomResponseDto;
import com.market.domain.chatRoom.entity.ChatRoom;
import com.market.domain.chatRoom.repository.ChatRoomRepository;
import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomRepository chatRoomRepository;
    private final MemberRepository memberRepository;

    @Override
    @Transactional // 채팅방 생성
    public ChatRoomResponseDto createChatRoom(ChatRoomRequestDto requestDto, Member member) {
        List<Member> receivers = memberRepository.findAllByRole(Role.ADMIN);

        if (receivers.isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_EXISTS_ADMIN);
        }

        // 만약 회원 제재 여부가 true 라면 채팅방 생성 불가
        if (member.isWarning()) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACTION);
        }

        // ChatRoom 생성 시 다수의 receivers 를 사용
        requestDto.setReceivers(receivers);
        ChatRoom chatRoom = requestDto.toEntity(member);

        chatRoomRepository.save(chatRoom);
        return ChatRoomResponseDto.of(chatRoom);
    }

    @Override
    @Transactional(readOnly = true)  // 채팅방 목록 조회
    public Page<ChatRoomResponseDto> getChatRooms(Pageable pageable) {
        Page<ChatRoom> chatRoomList = chatRoomRepository.findAllByOrderByCreateTimeDesc(pageable);
        return chatRoomList.map(ChatRoomResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true)  // 내 채팅방 목록 조회
    public Page<ChatRoomResponseDto> getMyChatRooms(Member member, Pageable pageable) {
        Page<ChatRoom> chatRoomList = chatRoomRepository.findAllByMember_MemberNoOrderByCreateTimeDesc(
            member.getMemberNo(), pageable);
        return chatRoomList.map(ChatRoomResponseDto::of);
    }

    @Override
    @Transactional(readOnly = true)  // 채팅방 단건 조회
    public ChatRoomResponseDto getChatRoom(Long chatRoomNo) {
        ChatRoom chatRoom = findChatRoom(chatRoomNo);
        return ChatRoomResponseDto.of(chatRoom);
    }

    @Override
    @Transactional // 채팅방 삭제
    public void deleteChatRoom(Long chatRoomNo, Member member) {
        validateIsMasterAndAdmin(chatRoomNo, member);
        ChatRoom chatRoom = findChatRoom(chatRoomNo);
        chatRoomRepository.delete(chatRoom);
    }

    @Override
    @Transactional(readOnly = true) // 채팅방 찾기
    public ChatRoom findChatRoom(Long chatRoomNo) {
        return chatRoomRepository.findById(chatRoomNo).orElseThrow(() ->
            new BusinessException(ErrorCode.NOT_FOUND_CHATROOM));
    }

    @Override
    @Transactional // 관리자 채팅방 읽은 상태로 변경
    public void markChatRoomAsRead(Long chatRoomNo, Member member) {
        validateIsAdmin(member);
        ChatRoom chatRoom = findChatRoom(chatRoomNo);
        chatRoom.markAsRead();
    }

    @Override
    @Transactional(readOnly = true)  // 채팅방 읽음 상태 조회
    public ChatRoomResponseDto getChatRoomIsRead(Long chatRoomNo) {
        ChatRoom chatRoom = findChatRoom(chatRoomNo);
        return ChatRoomResponseDto.of(chatRoom);
    }

    @Override
    @Transactional // 채팅방 읽지 않은 상태로 변경
    public void markChatRoomAsUnRead(Long chatRoomNo, Member member) {
        validateIsAdmin(member);
        ChatRoom chatRoom = findChatRoom(chatRoomNo);
        chatRoom.markAsUnread();
    }

    @Override // 작성자거나 관리자인지 확인
    public void validateIsMasterAndAdmin(Long chatRoomNo, Member member) {
        boolean isOwner = chatRoomRepository.existsByNoAndMember_MemberNo(chatRoomNo,
            member.getMemberNo());
        if (!isOwner && !member.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.ONLY_MASTER_AND_ADMIN_HAVE_AUTHORITY);
        }
    }

    @Override // 관리자인지 확인
    public void validateIsAdmin(Member member) {
        if (!member.getRole().equals(Role.ADMIN)) {
            throw new BusinessException(ErrorCode.ONLY_ADMIN_HAVE_AUTHORITY);
        }
    }
}