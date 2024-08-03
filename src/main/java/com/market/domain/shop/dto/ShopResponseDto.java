package com.market.domain.shop.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.item.dto.ItemResponseDto;
import com.market.domain.shop.entity.CategoryEnum;
import com.market.domain.shop.entity.Shop;
import com.market.domain.shop.shopComment.dto.ShopCommentResponseDto;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
public class ShopResponseDto {
    private Long shopNo;

    private String shopName;

    private String tel;

    private String sellerName;

    private String shopAddr;

    private String shopLat;

    private String shopLng;

    private CategoryEnum category;

    private Integer likes;

    private Long viewCount; // 조회수

    private Long marketNo; // 소속 시장 알기 위한 필드

    private List<ImageResponseDto> imageList;

    private List<ItemResponseDto> itemList;

    private List<ShopCommentResponseDto> shopCommentList;

    public static ShopResponseDto of(Shop shop) { // TODO : member 정보 추가
        return ShopResponseDto.builder()
            .shopNo(shop.getNo())
            .shopName(shop.getShopName())
            .tel(shop.getTel())
            .sellerName(shop.getSellerName())
            .shopAddr(shop.getShopAddr())
            .shopLat(shop.getShopLat())
            .shopLng(shop.getShopLng())
            .category(shop.getCategory())
            .likes(shop.getShopLikeList().size())
            .viewCount(shop.getViewCount())
            .marketNo(shop.getMarket().getNo())
            .imageList(shop.getImageList().stream().map(ImageResponseDto::of).toList())
            .itemList(shop.getItemList().stream().map(ItemResponseDto::of).toList())
            .shopCommentList(
                shop.getShopCommentList().stream().map(ShopCommentResponseDto::of).toList())
            .build();
    }
}
