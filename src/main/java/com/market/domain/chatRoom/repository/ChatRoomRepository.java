package com.market.domain.chatRoom.repository;

import com.market.domain.chatRoom.entity.ChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    Page<ChatRoom> findAllByOrderByCreateTimeDesc(Pageable pageable);

    Page<ChatRoom> findAllByMember_MemberNoOrderByCreateTimeDesc(Long MemberNo, Pageable pageable);

    boolean existsByNoAndMember_MemberNo(Long chatRoomNo, Long memberNo);
}