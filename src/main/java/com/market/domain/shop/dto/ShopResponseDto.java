package com.market.domain.shop.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.item.dto.ItemResponseDto;
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

    private String postCode;

    private String streetAddr;

    private String detailAddr;

    private Integer likes;

    private List<ImageResponseDto> imageList;

    private List<ItemResponseDto> itemList;

    private List<ShopCommentResponseDto> shopCommentList;

    public static ShopResponseDto of(Shop shop) { // TODO : member 정보 추가
        return ShopResponseDto.builder()
            .shopNo(shop.getNo())
            .shopName(shop.getShopName())
            .tel(shop.getTel())
            .sellerName(shop.getSellerName())
            .postCode(shop.getPostCode())
            .streetAddr(shop.getStreetAddr())
            .detailAddr(shop.getDetailAddr())
            .likes(shop.getShopLikeList().size())
            .imageList(shop.getImageList().stream().map(ImageResponseDto::of).toList())
            .itemList(shop.getItemList().stream().map(ItemResponseDto::of).toList())
            .shopCommentList(
                shop.getShopCommentList().stream().map(ShopCommentResponseDto::of).toList())
            .build();
    }
}
