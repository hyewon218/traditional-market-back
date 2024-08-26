package com.market.domain.chat.service;

import com.market.domain.chat.dto.ChatMessageDto;
import com.market.domain.chat.dto.ChatResponseDto;
import com.market.domain.chat.entity.Chat;
import com.market.domain.chat.repository.ChatRepository;
import com.market.domain.chatRoom.entity.ChatRoom;
import com.market.domain.chatRoom.repository.ChatRoomRepository;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import java.util.List;

import com.market.global.profanityFilter.ProfanityFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 메세지 삭제 - DB Scheduler 적용 필요
    @Override
    @Transactional(readOnly = true) // 채팅방 채팅 목록 조회
    public List<ChatResponseDto> getAllChatByRoomId(Long roomId) {
        List<Chat> chatList = chatRepository.findAllByChatRoomNoOrderByCreateTimeAsc(roomId);
        return chatList.stream().map(ChatResponseDto::of).toList();
    }

    @Override
    @Transactional // 채팅 메세지 저장
    public void saveMessage(Long roomId, ChatMessageDto requestDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CHATROOM));
        validateProfanity(requestDto.getMessage());
        Chat chat = requestDto.toEntity(chatRoom);
        chatRepository.save(chat);
    }

    @Override // 전송하려는 채팅 메세지에 비속어 포함되어 있는지 검증
    public void validateProfanity(String message) {
        if (ProfanityFilter.containsProfanity(message)) {
            throw new BusinessException(ErrorCode.NOT_ALLOW_PROFANITY_CHAT);
        }
    }
}