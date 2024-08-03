package com.market.domain.shop.repository;

import com.market.domain.shop.entity.CategoryEnum;
import com.market.domain.shop.entity.Shop;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ShopRepository extends JpaRepository<Shop, Long> {

    Page<Shop> findAll(Pageable pageable);

    Page<Shop> findAllByMarket_No(Long marketNo, Pageable pageable);

    Page<Shop> findByCategoryOrderByCategoryDesc(CategoryEnum category, Pageable pageable);

    Page<Shop> findByMarketNoAndCategory(Long marketNo, CategoryEnum category, Pageable pageable);
}