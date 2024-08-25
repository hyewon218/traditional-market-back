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

    // 마스킹 아이디 사용 시 해제하기
//    public static ShopCommentResponseDto of(ShopComment shopComment){
//        String maskingUsername = idMasking(shopComment.getMember().getMemberId());
//        return ShopCommentResponseDto.builder()
//            .id(shopComment.getNo())
//            .comment(shopComment.getComment())
//            .shopName(shopComment.getShop().getShopName())
//            .username(maskingUsername)
//            .createTime(shopComment.getCreateTime())
//            .updateTime(shopComment.getUpdateTime())
//            .build();
//    }

    // 4 범위 뒤로는 모두 마스킹 처리
    public static String idMasking(String username) {
        return username.replaceAll("(?<=.{4}).", "*");
    }
}
