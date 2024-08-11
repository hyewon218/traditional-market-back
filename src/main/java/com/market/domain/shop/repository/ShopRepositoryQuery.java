package com.market.domain.shop.repository;

import com.market.domain.market.entity.Market;
import com.market.domain.shop.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ShopRepositoryQuery {

    Page<Shop> searchShops(ShopSearchCond cond, Pageable pageable);
}