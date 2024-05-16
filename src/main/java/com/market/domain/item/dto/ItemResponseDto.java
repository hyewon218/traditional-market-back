package com.market.domain.item.dto;

import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ItemResponseDto {

    private String itemName;

    private int price;

    private int stockNumber;

    private String itemDetail;

    private ItemSellStatus itemSellStatus;

    public static ItemResponseDto of(Item item) {
        return ItemResponseDto.builder()
            .itemName(item.getItemName())
            .price(item.getPrice())
            .stockNumber(item.getStockNumber())
            .itemDetail(item.getItemDetail())
            .itemSellStatus(item.getItemSellStatus())
            .build();
    }
}
