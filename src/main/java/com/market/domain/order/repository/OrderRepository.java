package com.market.domain.order.repository;

import com.market.domain.member.entity.Member;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.entity.Order;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    // BaseEntity 의 createTime 필드를 이용하여 가장 최근 주문을 조회하는 메서드 (결제 전 주문페이지)
    Optional<Order> findFirstByMemberOrderByCreateTimeDesc(Member member);

    // 결제 완료된 주문 중 가장 최근 주문 (결제 성공 후 주문 상세페이지)
    @Query(value = "select o " +
        "from Order o " +
        "where o.member.memberNo = :memberNo " +
        "and o.orderStatus = :orderStatus " +
        "order by o.orderDate desc " +
        "limit 1"
    )
    Order findLatestOrder(@Param("memberNo") Long memberNo,
        @Param("orderStatus") OrderStatus orderStatus);

    // 결제 완료된 주문 목록
    // 로그인한 사용자의 주문 데이터를 페이징 조건에 맞춰 조회 + 주문 갯수 조회
    @Query(value = "select o " +
        "from Order o " +
        "where o.member.memberNo = :memberNo " +
        "and o.orderStatus = :orderStatus " +
        "order by o.orderDate desc ",
        countQuery = "select count(o) " +
            "from Order o " +
            "where o.member.memberNo = :memberNo ")
    // 성능 저하로 countQuery 분리
    Page<Order> findOrdersByMemberWithPaging(@Param("memberNo") Long memberNo,
        @Param("orderStatus") OrderStatus orderStatus, Pageable pageable);

    // 삭제될 ORDER 주문 목록
    @Query(value = "select o " +
        "from Order o " +
        "where o.member.memberNo = :memberNo " +
        "and o.orderStatus = :orderStatus ")
    List<Order> findStatusOrders(@Param("memberNo") Long memberNo,
        @Param("orderStatus") OrderStatus orderStatus);

    // 주문번호로 주문찾기
    @Query("select o " +
        "from Order o join fetch o.member m " +
        "join fetch o.orderItemList oi " +
        "join fetch oi.item " +
        "where o.no = :orderNo " +
        "order by o.orderDate desc ")
    Optional<Order> findOrderDetailsByNo(@Param("orderNo") Long orderNo);

    // 주문자 맞는지 확인
    boolean existsByNoAndMember_MemberNo(Long orderNo, Long memberNo);
}