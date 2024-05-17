package com.market.domain.market.marketComment.service;

import com.market.domain.market.marketComment.dto.MarketCommentRequestDto;
import com.market.domain.market.marketComment.entity.MarketComment;
import com.market.domain.member.entity.Member;

public interface MarketCommentService {

    void createMarketComment(MarketCommentRequestDto itemCommentRequestDto, Member member);

    void updateMarketComment(Long commentNo, MarketCommentRequestDto marketCommentRequestDto,
        Member member);

    void deleteMarketComment(Long commentNo, Member member);

    MarketComment findMarketComment(Long no);
}