package com.market.domain.market.dto;

import com.market.domain.market.entity.Market;
import com.market.domain.shop.dto.ShopResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MarketResponseDto {
    private String marketName;

    private String marketAddr;

    private String marketDetail;

    private List<ShopResponseDto> shopList;

    public static MarketResponseDto of(Market market) {
        return MarketResponseDto.builder()
            .marketName(market.getMarketName())
            .marketAddr(market.getMarketAddr())
            .marketDetail(market.getMarketDetail())
            .shopList(market.getShopList().stream().map(ShopResponseDto::of).toList())
            .build();
    }
}