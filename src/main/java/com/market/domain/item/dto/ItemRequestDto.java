package com.market.domain.item.dto;

import com.market.domain.item.constant.ItemSellStatus;
import com.market.domain.item.entity.Item;
import com.market.domain.shop.entity.Shop;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class ItemRequestDto {

    private long shopNo; // 소속 상점 no

    private String itemName;

    private int price;

    private int stockNumber;

    private String itemDetail;

    private ItemSellStatus itemSellStatus;

    public Item toEntity(Shop shop) {
        return Item.builder()
            .shop(shop)
            .itemName(this.itemName)
            .price(this.price)
            .stockNumber(this.stockNumber)
            .itemDetail(this.itemDetail)
            .itemSellStatus(this.itemSellStatus)
            .build();
    }
}
