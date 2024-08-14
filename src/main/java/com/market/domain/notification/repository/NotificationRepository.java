package com.market.domain.notification.repository;

import com.market.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByMember_MemberNo(Long memberNo, Pageable pageable);

    Long countByMember_MemberNo(Long userNo);
}