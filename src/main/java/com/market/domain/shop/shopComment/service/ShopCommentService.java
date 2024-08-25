package com.market.domain.shop.shopComment.service;

import com.market.domain.member.entity.Member;
import com.market.domain.shop.shopComment.dto.ShopCommentRequestDto;
import com.market.domain.shop.shopComment.dto.ShopCommentResponseDto;
import com.market.domain.shop.shopComment.entity.ShopComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopCommentService {

    void createShopComment(ShopCommentRequestDto shopCommentRequestsDto, Member member);

    Page<ShopCommentResponseDto> getShopComments(Long shopNo, Pageable pageable);

    void updateShopComment(Long commentNo, ShopCommentRequestDto shopCommentRequestsDto,
        Member member);

    void deleteShopComment(Long commentNo, Member member);

    ShopComment findShopComment(Long no);

    void validationProfanity(String comment); // 댓글에 비속어 포함되어있는지 검증
}
