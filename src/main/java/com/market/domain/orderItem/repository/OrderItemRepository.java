package com.market.domain.orderItem.repository;

import com.market.domain.orderItem.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findAllByOrderNo(Long orderNo);
}