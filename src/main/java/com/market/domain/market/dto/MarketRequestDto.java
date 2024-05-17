package com.market.domain.market.dto;

import com.market.domain.market.entity.Market;
import jakarta.validation.constraints.NotBlank;
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

    @NotBlank(message = "시장 상세는 필수 입력 값입니다.")
    private String marketDetail;

    public Market toEntity() {
        return Market.builder()
            .marketName(this.marketName)
            .marketAddr(this.marketAddr)
            .marketDetail(this.marketDetail)
            .build();
    }
}