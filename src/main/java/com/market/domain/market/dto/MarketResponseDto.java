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

    private Long totalSalesPrice; // 해당 시장에 속한 상점들의 총 매출액 합계

    private Long viewCount;

    private String parkingInfo1;

    private String parkingInfo2;

    private String busInfo;

//    private String busAddr;
    private String busLat; // 버스 정류장의 위도값

    private String busLng; // 버스 정류장의 경도값

    private String subwayInfo;

//    private String subwayAddr;
    private String subwayLat; // 지하철역 출구의 위도값

    private String subwayLng; // 지하철역 출구의 경도값

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
            .totalSalesPrice(market.getTotalSalesPrice())
            .viewCount(market.getViewCount())
            .parkingInfo1(market.getParkingInfo1())
            .parkingInfo2(market.getParkingInfo2())
            .busInfo(market.getBusInfo())
            .busLat(market.getBusLat())
            .busLng(market.getBusLng())
            .subwayInfo(market.getSubwayInfo())
            .subwayLat(market.getSubwayLat())
            .subwayLng(market.getSubwayLng())
            .shopList(market.getShopList().stream().map(ShopResponseDto::of).toList())
            .imageList(market.getImageList().stream().map(ImageResponseDto::of).toList())
            .commentList(market.getMarketCommentList().stream().map(MarketCommentResponseDto::of).toList())
            .build();
    }
}