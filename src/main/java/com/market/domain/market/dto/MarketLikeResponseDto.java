package com.market.domain.market.dto;

import com.market.domain.image.dto.ImageResponseDto;
import com.querydsl.core.annotations.QueryProjection;
import java.util.List;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
public class MarketLikeResponseDto {

    private Long marketNo;

    private String marketName;

    private Long likes;

    private List<ImageResponseDto> imageUrl;


    @QueryProjection
    public MarketLikeResponseDto(Long marketNo, String marketName, Long likes,
        List<ImageResponseDto> imageUrl) {
        this.marketNo = marketNo;
        this.marketName = marketName;
        this.likes = likes;
        this.imageUrl = imageUrl;
    }
}