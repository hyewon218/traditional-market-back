package com.market.domain.orderItem.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.orderItem.entity.OrderItem;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class OrderItemHistResponseDto {

    private String itemName; // 상품명

    private int count; // 주문 수량

    private int orderPrice; // 가격

    private List<ImageResponseDto> imageList; // 상품 이미지

    public static OrderItemHistResponseDto of(OrderItem orderItem) {
        return OrderItemHistResponseDto.builder()
            .itemName(orderItem.getItem().getItemName())
            .count(orderItem.getCount())
            .orderPrice(orderItem.getOrderPrice())
            .imageList(
                orderItem.getItem().getImageList().stream().map(ImageResponseDto::of).toList())
            .build();
    }
}