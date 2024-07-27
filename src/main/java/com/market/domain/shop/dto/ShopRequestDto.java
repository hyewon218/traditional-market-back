package com.market.domain.shop.dto;

import com.market.domain.market.entity.Market;
import com.market.domain.member.entity.Member;
import com.market.domain.shop.entity.CategoryEnum;
import com.market.domain.shop.entity.Shop;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
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
public class ShopRequestDto {

    @NotBlank(message = "시장 No는 필수 입력 값입니다.")
    private Long marketNo; // 소속 시장 no

    private Long sellerNo;

    @NotBlank(message = "상점명은 필수 입력 값입니다.")
    private String shopName;

    @NotBlank(message = "상점 전화번호는 필수 입력 값입니다.")
    @Pattern(regexp = "^01(?:0|1|[6-9])[.-]?(\\d{3}|\\d{4})[.-]?(\\d{4})$", message = "10 ~ 11 자리의 숫자만 입력 가능합니다.")
    private String tel;

    @NotBlank(message = "상점 사장님 이름은 필수 입력 값입니다.")
    private String sellerName;

    @NotBlank(message = "상점 주소는 필수 입력 값입니다.")
    private String shopAddr;

    @NotBlank(message = "카테고리는 필수 입력 값입니다.")
    private CategoryEnum category;

    private List<String> imageUrls; // 상점 수정 화면에서 남은 기존 이미지들


    public Shop toEntity(Market market) {
        return Shop.builder()
            .market(market)
            .shopName(this.shopName)
            .tel(this.tel)
            .sellerName(this.sellerName)
            .shopAddr(this.shopAddr)
            .category(this.category)
            .viewCount(0L)
            .build();
    }
    public Shop toEntity(Market market, Member seller) {
        return Shop.builder()
            .market(market)
            .seller(seller)
            .shopName(this.shopName)
            .tel(this.tel)
            .sellerName(this.sellerName)
            .shopAddr(this.shopAddr)
            .category(this.category)
            .viewCount(0L)
            .build();
    }
}
