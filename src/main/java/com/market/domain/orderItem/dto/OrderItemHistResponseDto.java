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

    private Long orderItemNo; // 주문상품 No

    private String itemName; // 상품명

    private int count; // 주문 수량

    private int orderPrice; // 가격

    private List<ImageResponseDto> imageList; // 상품 이미지

    private Long itemNo; // 상품, 주문 내역 페이지에서 상품 이미지 누르면 해당 상품의 상세 정보로 이동하기 위해 필드 추가

    public static OrderItemHistResponseDto of(OrderItem orderItem) {
        return OrderItemHistResponseDto.builder()
            .orderItemNo(orderItem.getNo())
            .itemName(orderItem.getItem().getItemName())
            .count(orderItem.getCount())
            .orderPrice(orderItem.getOrderPrice())
            .imageList(
                orderItem.getItem().getImageList().stream().map(ImageResponseDto::of).toList())
            .itemNo(orderItem.getItem().getNo())
            .build();
    }
}