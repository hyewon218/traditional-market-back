package com.market.domain.shop.shopComment.service;

import com.market.domain.member.entity.Member;
import com.market.domain.shop.shopComment.dto.ShopCommentRequestDto;
import com.market.domain.shop.shopComment.entity.ShopComment;

public interface ShopCommentService {

    void createShopComment(ShopCommentRequestDto shopCommentRequestsDto, Member member);

    void updateShopComment(Long commentNo, ShopCommentRequestDto shopCommentRequestsDto,
        Member member);

    void deleteShopComment(Long commentNo, Member member);

    ShopComment findShopComment(Long no);
}
