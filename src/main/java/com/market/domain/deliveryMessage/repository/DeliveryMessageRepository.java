package com.market.domain.deliveryMessage.repository;

import com.market.domain.deliveryMessage.entity.DeliveryMessage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DeliveryMessageRepository extends JpaRepository<DeliveryMessage, Long> {

    List<DeliveryMessage> findAllByMemberNo(long memberNo);
}
