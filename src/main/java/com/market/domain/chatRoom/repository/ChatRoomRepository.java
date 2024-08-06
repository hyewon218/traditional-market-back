package com.market.domain.chatRoom.repository;

import com.market.domain.chatRoom.entity.ChatRoom;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    List<ChatRoom> findAllByOrderByCreateTimeDesc();
    List<ChatRoom> findAllByMember_MemberNoOrderByCreateTimeDesc(Long id);
}