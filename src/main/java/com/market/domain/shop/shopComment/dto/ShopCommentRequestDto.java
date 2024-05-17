package com.market.domain.shop.shopComment.dto;

import com.market.domain.member.entity.Member;
import com.market.domain.shop.entity.Shop;
import com.market.domain.shop.shopComment.entity.ShopComment;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class ShopCommentRequestDto {

    private long shopNo; // 댓글 남길 상점 no
    private String comment;

    public ShopComment toEntity(Shop shop, Member member) {
        return ShopComment.builder()
            .comment(this.comment)
            .shop(shop)
            .member(member)
            .build();
    }
}
