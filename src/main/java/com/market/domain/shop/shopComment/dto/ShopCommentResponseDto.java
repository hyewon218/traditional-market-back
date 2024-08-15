package com.market.domain.shop.shopComment.dto;

import com.market.domain.shop.shopComment.entity.ShopComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class ShopCommentResponseDto {

    private Long id;

    private String comment;

    private String shopName;

    private String username;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static ShopCommentResponseDto of(ShopComment shopComment) {
        return ShopCommentResponseDto.builder()
            .id(shopComment.getNo())
            .comment(shopComment.getComment())
            .shopName(shopComment.getShop().getShopName())
            .username(shopComment.getMember().getMemberId())
            .createTime(shopComment.getCreateTime())
            .updateTime(shopComment.getUpdateTime())
            .build();
    }
}
