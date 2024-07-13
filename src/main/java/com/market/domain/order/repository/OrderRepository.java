package com.market.domain.order.repository;

import com.market.domain.member.entity.Member;
import com.market.domain.order.entity.Order;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // 로그인한 사용자의 주문 데이터를 페이징 조건에 맞춰 조회 + 주문 갯수 조회
    @Query(value = "select o " +
        "from Order o " +
        "where o.member.memberId = :memberId " +
        "order by o.orderDate desc ",
        countQuery = "select count(o) " +
            "from Order o " +
            "where o.member.memberId = :memberId ") // 성능 저하로 countQuery 분리
    Page<Order> findOrderListWithMember(@Param("memberId") String memberId, Pageable pageable);

    @Query("select o " +
        "from Order o join fetch o.member m " +
        "join fetch o.orderItemList oi " +
        "join fetch oi.item " +
        "where o.no = :orderNo " +
        "order by o.orderDate desc ")
    Optional<Order> findByOrderNoWithMemberAndOrderItemListAndItem(@Param("orderNo") Long orderNo);

    // BaseEntity 의 createTime 필드를 이용하여 가장 최근 주문을 조회하는 메서드
    Optional<Order> findFirstByMemberOrderByCreateTimeDesc(Member member);
}