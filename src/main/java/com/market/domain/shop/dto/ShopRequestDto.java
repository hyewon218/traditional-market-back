package com.market.domain.shop.dto;

import com.market.domain.market.entity.Market;
import com.market.domain.member.entity.Member;
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

    @NotBlank(message = "시장 No는 필수 입력 값입니다.")
    private Long marketNo; // 소속 시장 no

    private Long sellerNo;

    @NotBlank(message = "상점명은 필수 입력 값입니다.")
    private String shopName;

    @NotBlank(message = "상점 번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$", message = "10 ~ 11 자리의 숫자만 입력 가능합니다.")
    private String tel;

    @NotBlank(message = "상점 사장님 이름은 필수 입력 값입니다.")
    private String sellerName;

    @NotBlank(message = "상점 우편번호는 필수 입력 값입니다.")
    private String postCode;

    @NotBlank(message = "상점 도로명주소는 필수 입력 값입니다.")
    private String streetAddr;

    @NotBlank(message = "상점 상세주소는 필수 입력 값입니다.")
    private String detailAddr;


    public Shop toEntity(Market market) {
        return Shop.builder()
            .market(market)
            .shopName(this.shopName)
            .tel(this.tel)
            .sellerName(this.sellerName)
            .postCode(this.postCode)
            .streetAddr(this.streetAddr)
            .detailAddr(this.detailAddr)
            .build();
    }
    public Shop toEntity(Market market, Member seller) {
        return Shop.builder()
            .market(market)
            .seller(seller)
            .shopName(this.shopName)
            .tel(this.tel)
            .sellerName(this.sellerName)
            .postCode(this.postCode)
            .streetAddr(this.streetAddr)
            .detailAddr(this.detailAddr)
            .build();
    }
}
