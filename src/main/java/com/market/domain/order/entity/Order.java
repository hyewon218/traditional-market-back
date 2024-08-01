package com.market.domain.order.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.member.entity.Member;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.dto.SaveDeliveryRequestDto;
import com.market.domain.orderItem.entity.OrderItem;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "orders") // order = 예약된 키워드
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_no")
    private Long no;

    private LocalDateTime orderDate; // 주문일

    private String tid; // 카카오페이 결제고유번호, 결제 승인되면 생성됨 / 결제 취소할 때 해당값 필요

    @Enumerated(EnumType.STRING)
    private OrderStatus orderStatus; // 주문 상태

    private String deliveryAddr; // 결제 시 선택한 배송지

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_no")
    private Member member;

    @Builder.Default
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.LAZY)
    private List<OrderItem> orderItemList = new ArrayList<>(); // 장바구니 페이지에서 한 번에 여러개 주문 가능

    private boolean isCartOrder; // 장바구니 주문 여부

    public void setOder(OrderItem orderItem) {
        orderItem.setOrder(this); // orderItem 객체에 order 객체 세팅(양방향 참조)
    }

    public void setTid(String tid) { // tid(카카오페이 결제고유번호) 저장 메서드
        this.tid = tid;
    }

    public void setDelivery(SaveDeliveryRequestDto saveDeliveryRequestDto) {
        this.deliveryAddr = saveDeliveryRequestDto.getDeliveryAddr();
    }

    public static Order toEntity(Member member, List<OrderItem> orderItemList, Boolean isCartOrder) {
        Order order = Order.builder()
            .member(member)
            .orderItemList(orderItemList)
            .orderStatus(OrderStatus.ORDER)
            .orderDate(LocalDateTime.now())
            .isCartOrder(isCartOrder)
            .build();
        for (OrderItem orderItem : orderItemList) {
            order.setOder(orderItem);
        }
        return order;
    }

    public int getTotalPrice() {
        return this.orderItemList.stream()
            .mapToInt(OrderItem::getTotalPrice).sum();
    }

    public void cancelOrder() {
        this.orderStatus = OrderStatus.CANCEL;
        this.orderItemList.forEach(OrderItem::cancelOrder);
    }

    public void setOrderComplete() { // 결제 승인 시 주문 상태 변경
        this.orderStatus = OrderStatus.COMPLETE;
    }

    public void statusOrderAddStock() { // 주문 상태 ORDER 인 목록 orderItem 재고 증가
        this.orderItemList.forEach(OrderItem::cancelOrder);
    }
}
