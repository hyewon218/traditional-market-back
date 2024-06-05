package com.market.domain.chat.repository;

import com.market.domain.chat.entity.Chat;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRepository extends JpaRepository<Chat, Long> {
    List<Chat> findAllByChatRoomNoOrderByCreateTimeAsc(Long roomId); // 방 메세지 생성날짜 기준 오름차순 정렬
}