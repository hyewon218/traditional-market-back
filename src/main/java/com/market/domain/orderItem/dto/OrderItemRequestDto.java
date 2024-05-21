package com.market.domain.orderItem.dto;

import com.market.domain.item.entity.Item;
import com.market.domain.orderItem.entity.OrderItem;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OrderItemRequestDto {

    @NotNull(message = "상품 아이디는 필수 입력 값입니다.")
    private Long itemNo;

    @Min(value = 1, message = "최소 주문 수량은 1개 입니다.")
    @Max(value = 999, message = "최대 주문 수량은 999개 입니다.")
    private int count; // 주문 수량

    public OrderItem toEntity(Item item) {
        // 주문 상품을 생성한다는 것은 수량만큼 상품의 재고를 차감하는 것
        item.removeStock(count);

        return OrderItem.builder()
            .orderPrice(item.getPrice())
            .count(count)
            .item(item)
            .build();
    }
}