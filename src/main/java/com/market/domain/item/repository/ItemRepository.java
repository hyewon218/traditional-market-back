package com.market.domain.item.repository;

import com.market.domain.item.entity.Item;
import com.market.domain.item.entity.ItemCategoryEnum;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ItemRepository extends JpaRepository<Item, Long> {

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT i FROM Item i WHERE i.no = :no")
    Optional<Item> findByIdWithLock(@Param("no") Long no);

    Page<Item> findAll(Pageable pageable);

    Page<Item> findAllByShop_No(Long shopNo, Pageable pageable);

    Page<Item> findByShopNoAndItemCategory(Long shopNo, ItemCategoryEnum itemCategory,
        Pageable pageable);

    List<Item> findByShopMarketNoAndItemCategory(Long marketNo, ItemCategoryEnum itemCategory);

    Page<Item> findByShopMarketNoAndItemCategory(Long marketNo, ItemCategoryEnum itemCategory,
        Pageable pageable);

    Page<Item> findByItemCategoryOrderByItemCategoryDesc(ItemCategoryEnum itemCategory,
        Pageable pageable);

    Page<Item> findAllByShop_Market_No(Long marketNo, Pageable pageable);

    Item findByShopNoAndNo(Long shopNo, Long itemNo);
}