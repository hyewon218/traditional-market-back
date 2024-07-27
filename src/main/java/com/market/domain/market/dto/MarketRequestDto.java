package com.market.domain.market.dto;

import com.market.domain.market.entity.CategoryEnum;
import com.market.domain.market.entity.Market;
import jakarta.validation.constraints.NotBlank;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Setter
public class MarketRequestDto {

    @NotBlank(message = "시장명은 필수 입력 값입니다.")
    private String marketName;

    @NotBlank(message = "시장 주소는 필수 입력 값입니다.")
    private String marketAddr;

    @NotBlank(message = "카테고리는 필수 입력 값입니다.")
    private CategoryEnum category;

    @NotBlank(message = "시장 상세는 필수 입력 값입니다.")
    private String marketDetail;

    private List<String> imageUrls; // 시장 수정 화면에서 남은 기존 이미지들

    public Market toEntity() {
        return Market.builder()
            .marketName(this.marketName)
            .marketAddr(this.marketAddr)
            .category(this.category)
            .marketDetail(this.marketDetail)
            .viewCount(0L)
            .build();
    }
}