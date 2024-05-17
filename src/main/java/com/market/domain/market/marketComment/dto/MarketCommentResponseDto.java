package com.market.domain.market.marketComment.dto;

import com.market.domain.market.marketComment.entity.MarketComment;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MarketCommentResponseDto {

    private Long id;

    private String comment;

    private String marketName;

    private String username;

    public static MarketCommentResponseDto of(MarketComment marketComment){
        return MarketCommentResponseDto.builder()
                .id(marketComment.getNo())
                .comment(marketComment.getComment())
                .marketName(marketComment.getMarket().getMarketName())
                .username(marketComment.getMember().getMemberId())
                .build();
    }
}