package com.market.domain.order.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.member.entity.Member;
import com.market.domain.orderItem.constant.OrderStatus;
import com.market.domain.orderItem.entity.OrderItem;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_no")
    private Long no;

    private LocalDateTime orderDate; // 주문일

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문 상태

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no")
    private Member member;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItemList = new ArrayList<>(); // 장바구니 페이지에서 한 번에 여러개 주문 가능


    public void setOder(OrderItem orderItem) {
        orderItem.setOrder(this); // orderItem 객체에 order 객체 세팅(양방향 참조)
    }

    public static Order toEntity(Member member, List<OrderItem> orderItemList) {
        Order order = new Order();

        for (OrderItem orderItem : orderItemList) {
            order.setOder(orderItem);
        }

        return Order.builder()
            .member(member)
            .orderItemList(orderItemList)
            .orderStatus(OrderStatus.ORDER)
            .orderDate(LocalDateTime.now())
            .build();
    }

    public int getTotalPrice() {
        return this.orderItemList.stream()
            .mapToInt(OrderItem::getTotalPrice).sum();
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;
        this.orderItemList.forEach(OrderItem::cancelOrder);
    }
}
