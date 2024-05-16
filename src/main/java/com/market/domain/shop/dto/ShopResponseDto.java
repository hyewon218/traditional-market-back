package com.market.domain.shop.dto;

import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.shop.entity.Shop;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ShopResponseDto {
    private String shopName;

    private String tel;

    private String owner;

    private String postCode;

    private String streetAddr;

    private String detailAddr;

    private List<ItemResponseDto> itemList;

    public static ShopResponseDto of(Shop shop) {
        return ShopResponseDto.builder()
            .shopName(shop.getShopName())
            .tel(shop.getTel())
            .owner(shop.getOwner())
            .postCode(shop.getPostCode())
            .streetAddr(shop.getStreetAddr())
            .detailAddr(shop.getDetailAddr())
            .itemList(shop.getItemList().stream().map(ItemResponseDto::of).toList())
            .build();
    }
}
