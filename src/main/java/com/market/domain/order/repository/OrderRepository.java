package com.market.domain.order.repository;

import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.entity.Order;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 삭제될 ORDER 주문 목록(전체)
    List<Order> findAllByOrderStatus(OrderStatus orderStatus);

    // 주문자 맞는지 확인
    boolean existsByNoAndMember_MemberNo(Long orderNo, Long memberNo);
}