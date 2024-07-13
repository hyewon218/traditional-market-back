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


    public static OrderItemHistResponseDto of(OrderItem orderItem) {
        return OrderItemHistResponseDto.builder()
            .orderItemNo(orderItem.getNo())
            .itemName(orderItem.getItem().getItemName())
            .count(orderItem.getCount())
            .orderPrice(orderItem.getOrderPrice())
            .imageList(
                orderItem.getItem().getImageList().stream().map(ImageResponseDto::of).toList())
            .build();
    }

    public OrderItemHistResponseDto(Long orderItemNo, String itemName, int count, int orderPrice) {
        this.orderItemNo = orderItemNo;
        this.itemName = itemName;
        this.count = count;
        this.orderPrice = orderPrice;
    }

    public void setImageList(List<ImageResponseDto> imageList) { // 이미지를 처리하는 로직은 서비스 레이어에서 처리
        this.imageList = imageList;
    }
}