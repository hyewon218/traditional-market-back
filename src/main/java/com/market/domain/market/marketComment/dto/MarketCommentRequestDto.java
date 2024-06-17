package com.market.domain.market.marketComment.dto;

import com.market.domain.market.entity.Market;
import com.market.domain.market.marketComment.entity.MarketComment;
import com.market.domain.member.entity.Member;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class MarketCommentRequestDto {

    private long marketNo; // 댓글 남길 시장 no
    private String comment;

    public MarketComment toEntity(Market market, Member member) {
        return MarketComment.builder()
            .comment(this.comment)
            .market(market)
            .member(member)
            .build();
    }
}