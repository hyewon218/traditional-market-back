package com.market.domain.market.marketLike.repository;

import com.market.domain.market.entity.Market;
import com.market.domain.market.marketLike.entity.MarketLike;
import com.market.domain.member.entity.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketLikeRepository extends JpaRepository<MarketLike, Long> {

    Optional<MarketLike> findByMarketAndMember(Market market, Member user);
}