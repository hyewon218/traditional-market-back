package com.market.domain.order.repository;

import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.entity.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface OrderRepositoryQuery {

    Order findOrder(Long MemberNo, OrderStatus orderStatus);

    Optional<Order> findLatestOrder(Long MemberNo, OrderStatus orderStatus);

    Page<Order> findOrders(Long memberNo, Pageable pageable);

    Page<Order> findCancelOrders(Long memberNo, Pageable pageable);

    Page<Order> findOrdersByAdminExcludingCanceled(Pageable pageable);

    Page<Order> findOrdersByAdminAndOrderStatus(OrderStatus orderStatus, Pageable pageable);

    Page<Order> findOrdersBySellerExcludingCanceled(Long memberNo, Pageable pageable);

    Page<Order> findOrdersBySellerAndOrderStatus(Long memberNo, OrderStatus orderStatus,
        Pageable pageable);

    Page<Order> searchOrders(OrderSearchCond cond, Pageable pageable);

    Page<Order> searchOrdersSeller(Long sellerNo, OrderSearchCond cond, Pageable pageable);
}