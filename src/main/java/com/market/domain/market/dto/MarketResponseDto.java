package com.market.domain.market.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.market.entity.CategoryEnum;
import com.market.domain.market.entity.Market;
import com.market.domain.market.marketComment.dto.MarketCommentResponseDto;
import com.market.domain.shop.dto.ShopResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class MarketResponseDto {
    private Long marketNo;

    private String marketName;

    private String marketAddr;

    private CategoryEnum category;

    private String marketDetail;

    private Integer likes;

    private Long viewCount;

    private List<ShopResponseDto> shopList;

    private List<ImageResponseDto> imageList;

    private List<MarketCommentResponseDto> commentList;

    public static MarketResponseDto of(Market market) {
        return MarketResponseDto.builder()
            .marketNo(market.getNo())
            .marketName(market.getMarketName())
            .marketAddr(market.getMarketAddr())
            .category(market.getCategory())
            .marketDetail(market.getMarketDetail())
            .likes(market.getMarketLikeList().size())
            .viewCount(market.getViewCount())
            .shopList(market.getShopList().stream().map(ShopResponseDto::of).toList())
            .imageList(market.getImageList().stream().map(ImageResponseDto::of).toList())
            .commentList(market.getMarketCommentList().stream().map(MarketCommentResponseDto::of).toList())
            .build();
    }
}