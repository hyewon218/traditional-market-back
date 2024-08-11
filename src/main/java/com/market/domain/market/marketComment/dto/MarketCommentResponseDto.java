package com.market.domain.market.marketComment.dto;

import com.market.domain.market.marketComment.entity.MarketComment;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MarketCommentResponseDto {

    private Long id;

    private String comment;

    private String marketName;

    private String username;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    public static MarketCommentResponseDto of(MarketComment marketComment){
        return MarketCommentResponseDto.builder()
                .id(marketComment.getNo())
                .comment(marketComment.getComment())
                .marketName(marketComment.getMarket().getMarketName())
                .username(marketComment.getMember().getMemberId())
                .createTime(marketComment.getCreateTime())
                .updateTime(marketComment.getUpdateTime())
                .build();
    }
}