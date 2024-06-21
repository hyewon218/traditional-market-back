package com.market.domain.item.repository;

import com.market.domain.item.entity.Item;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryQuery {

    Page<Item> searchItems(ItemSearchCond cond, Pageable pageable);
}