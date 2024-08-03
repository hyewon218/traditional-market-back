package com.market.domain.item.repository;

import com.market.domain.item.entity.Item;
import com.market.domain.item.entity.ItemCategoryEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    Page<Item> findAll(Pageable pageable);

    Page<Item> findAllByShop_No(Long shopNo, Pageable pageable);

    List<Item> findByShopMarketNoAndItemCategory(Long marketNo, ItemCategoryEnum itemCategory);
    Page<Item> findByShopMarketNoAndItemCategory(Long marketNo, ItemCategoryEnum itemCategory, Pageable pageable);

    List<Item> findByShopMarketNoAndItemName(Long marketNo, String itemName);

    Page<Item> findByItemCategoryOrderByItemCategoryDesc(ItemCategoryEnum itemCategory, Pageable pageable);

    Page<Item> findAllByShop_Market_No(Long marketNo, Pageable pageable);

}