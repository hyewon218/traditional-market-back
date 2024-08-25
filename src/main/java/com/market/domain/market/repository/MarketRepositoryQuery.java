package com.market.domain.market.repository;

import com.market.domain.market.dto.MarketLikeResponseDto;
import com.market.domain.market.entity.Market;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MarketRepositoryQuery {

    Page<Market> searchMarkets(MarketSearchCond cond, Pageable pageable);

    Page<MarketLikeResponseDto> findMarketsSortedByLikes(Pageable pageable);
}