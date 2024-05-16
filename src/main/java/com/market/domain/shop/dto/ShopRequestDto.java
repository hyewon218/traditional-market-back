package com.market.domain.shop.dto;

import com.market.domain.market.entity.Market;
import com.market.domain.shop.entity.Shop;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ShopRequestDto {

    private long marketNo; // 소속 시장 no

    private String shopName;

    @NotBlank
    @Pattern(regexp = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$", message = "10 ~ 11 자리의 숫자만 입력 가능합니다.")
    private String tel;

    private String owner;

    private String postCode;

    private String streetAddr;

    private String detailAddr;

    public Shop toEntity(Market market) {
        return Shop.builder()
            .market(market)
            .shopName(this.shopName)
            .tel(this.tel)
            .owner(this.owner)
            .postCode(this.postCode)
            .streetAddr(this.streetAddr)
            .detailAddr(this.detailAddr)
            .build();
    }
}
