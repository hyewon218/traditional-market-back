package com.market.domain.orderItem.entity;

import com.market.domain.base.BaseEntity;
import com.market.domain.item.entity.Item;
import com.market.domain.order.entity.Order;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "order_Item")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@AllArgsConstructor
public class OrderItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "order_item_no")
    private Long no;

    private int orderPrice; // 가격

    private int count; // 수량

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_no")
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "item_no")
    private Item item;

    public void setOrder(Order order) {
        this.order = order;
    }

    public int getTotalPrice() {
        return this.getOrderPrice() * this.getCount();
    }

    public void cancelOrder() {
        this.getItem().addStock(count);
    }
}
