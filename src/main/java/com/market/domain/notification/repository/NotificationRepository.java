package com.market.domain.notification.repository;

import com.market.domain.notification.entity.Notification;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    Page<Notification> findAllByReceiverNoOrderByCreateTimeDesc(Long memberNo, Pageable pageable);

    List<Notification> findAllByReceiverNoAndIsSent(Long memberNo, boolean sent);

    Long countByReceiverNoAndIsRead(Long userNo, boolean read);
}