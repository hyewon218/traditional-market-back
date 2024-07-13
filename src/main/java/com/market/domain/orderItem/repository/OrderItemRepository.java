package com.market.domain.orderItem.repository;

import com.market.domain.orderItem.dto.OrderItemHistResponseDto;
import com.market.domain.orderItem.entity.OrderItem;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    @Query( // DTO 의 생성자를 이용하여 반환 값으로 DTO 객체를 생성
        "select new com.market.domain.orderItem.dto.OrderItemHistResponseDto(oi.no, i.itemName, oi.count, i.price) "
            +
            "from OrderItem oi " +
            "join oi.item i " +
            "join oi.order o " +
            "join o.member m " +
            "where o.no = :orderNo and m.memberNo = :memberNo " +
            "order by oi.createTime desc"
    )
    List<OrderItemHistResponseDto> findOrderItemHistResponseDtoList(@Param("orderNo") Long orderNo,
        @Param("memberNo") Long memberNo);
}