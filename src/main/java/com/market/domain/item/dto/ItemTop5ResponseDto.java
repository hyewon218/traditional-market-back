package com.market.domain.item.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@AllArgsConstructor
// 상품 가격 저렴한 순으로 정렬한 5개 결과
public class ItemTop5ResponseDto {

    private int rank;
    private Long itemNo;
    private Long shopNo;
    private String shopName;
    private int price;

    // 랭크 설정
    public void setRank(int rank) {
        this.rank = rank;
    }
}
