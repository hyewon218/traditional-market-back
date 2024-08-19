package com.market.domain.item.repository;

import com.market.domain.item.dto.ItemTop5ResponseDto;
import com.market.domain.item.entity.Item;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface ItemRepositoryQuery {

    Page<Item> searchItems(ItemSearchCond cond, Pageable pageable);
    List<Item> searchRankingFiveItems(ItemSearchCond cond);
    List<ItemTop5ResponseDto> searchItemsByShopNoAndItemName(Long shopNo, String itemName);
}