package com.market.domain.market.marketComment.service;

import com.market.domain.market.marketComment.dto.MarketCommentRequestDto;
import com.market.domain.market.marketComment.dto.MarketCommentResponseDto;
import com.market.domain.market.marketComment.entity.MarketComment;
import com.market.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketCommentService {

    void createMarketComment(MarketCommentRequestDto itemCommentRequestDto, Member member);

    Page<MarketCommentResponseDto> getMarketComments(Long marketNo, Pageable pageable);

    void updateMarketComment(Long commentNo, MarketCommentRequestDto marketCommentRequestDto,
        Member member);

    void deleteMarketComment(Long commentNo, Member member);

    MarketComment findMarketComment(Long no);
}