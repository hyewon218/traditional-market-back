package com.market.domain.shop.shopComment.dto;

import com.market.domain.shop.shopComment.entity.ShopComment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class ShopCommentResponseDto {

    private Long id;

    private String comment;

    private String shopName;

    private String username;

    public static ShopCommentResponseDto of(ShopComment shopComment){
        return ShopCommentResponseDto.builder()
                .id(shopComment.getNo())
                .comment(shopComment.getComment())
                .shopName(shopComment.getShop().getShopName())
                .username(shopComment.getMember().getMemberId())
                .build();
    }
}
