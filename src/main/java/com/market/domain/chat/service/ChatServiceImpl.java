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
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ChatServiceImpl implements ChatService {

    private final ChatRepository chatRepository;
    private final ChatRoomRepository chatRoomRepository;

    // 메세지 삭제 - DB Scheduler 적용 필요
    // 채팅방 채팅 목록 조회
    @Override
    @Transactional(readOnly = true)
    public List<ChatResponseDto> getAllChatByRoomId(Long roomId) {
        List<Chat> ChatList = chatRepository.findAllByChatRoomNoOrderByCreateTimeAsc(roomId);
        return ChatList.stream().map(ChatResponseDto::of).toList();
    }

    // 채팅 메세지 저장
    @Override
    @Transactional
    public void saveMessage(Long roomId, ChatMessageDto requestDto) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
            .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND_CHATROOM));

        Chat chat = requestDto.toEntity(chatRoom);
        chatRepository.save(chat);
    }
}