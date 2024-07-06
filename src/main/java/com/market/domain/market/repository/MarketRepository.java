package com.market.domain.market.repository;

import com.market.domain.market.entity.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MarketRepository extends JpaRepository<Market, Long> {

    Page<Market> findAll(Pageable pageable);
    boolean existsMarketByMarketName(String marketName);

    Market findByMarketName(String marketName);
}