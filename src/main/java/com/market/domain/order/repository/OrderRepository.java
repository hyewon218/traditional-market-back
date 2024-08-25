package com.market.domain.order.repository;

import com.market.domain.member.entity.Member;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.entity.Order;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderRepository extends JpaRepository<Order, Long> {

    @Modifying // ORDER 주문 목록 전체 삭제
    @Query("DELETE FROM Order o WHERE o.orderStatus = :status")
    int deleteBatchByStatus(@Param("status") OrderStatus status); // 삭제된 행 수 반환

    // 주문자 맞는지 확인
    boolean existsByNoAndMember_MemberNo(Long orderNo, Long memberNo);

    // 주문 상태가 배송 완료 (FINISH)이면서 배송완료일 (finishDate)이 14일 이후인것 찾기
    List<Order> findAllByOrderStatusAndFinishDateBefore(OrderStatus orderStatus, LocalDateTime fourteenDaysAgo);
}