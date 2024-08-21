package com.market.domain.order.repository;

import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.entity.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryQuery {

    Order findOrder(Long MemberNo, OrderStatus orderStatus);

    Optional<Order> findLatestOrder(Long MemberNo, OrderStatus orderStatus);

    Page<Order> findCompleteOrders(Long memberNo, OrderStatus orderStatus, Pageable pageable);

    Page<Order> findOrdersBySellerExcludingCanceled(Long memberNo, OrderStatus orderStatus,
        Pageable pageable);

    Page<Order> findOrdersBySellerAndOrderStatus(Long memberNo, OrderStatus orderStatus,
        Pageable pageable);
}