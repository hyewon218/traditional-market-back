package com.market.domain.item.dto;

import com.market.domain.item.entity.Item;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
// 상품 가격 저렴한 순으로 정렬한 5개 결과
public class ItemTop5ResponseDto {

    private int rank;
    private String shopName;
    private int price;

    public static ItemTop5ResponseDto of(Item item) {
        return ItemTop5ResponseDto.builder()
                .shopName(item.getShop().getShopName())
                .price(item.getPrice())
                .build();
    }

    // 랭크 설정
    public void setRank(int rank) {
        this.rank = rank;
    }
}
