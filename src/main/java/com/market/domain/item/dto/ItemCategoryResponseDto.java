package com.market.domain.item.dto;

import com.market.domain.item.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
// 분류탭 눌렀을 경우 해당 분류에 대한 값 반환하는 Dto
public class ItemCategoryResponseDto {

    private String itemName;

    public static ItemCategoryResponseDto of(Item item) {
        return ItemCategoryResponseDto.builder()
                .itemName(item.getItemName())
                .build();
    }
}
