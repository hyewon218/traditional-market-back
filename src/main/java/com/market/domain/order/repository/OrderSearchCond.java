package com.market.domain.order.repository;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderSearchCond {
    private String keyword;
    private String type; // 추가된 타입, memberId, randomOrderId로 각각 검색
}