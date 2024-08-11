package com.market.domain.shop.repository;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ShopSearchCond {
    private String keyword;
}