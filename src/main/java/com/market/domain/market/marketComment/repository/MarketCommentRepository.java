package com.market.domain.market.marketComment.repository;

import com.market.domain.market.marketComment.entity.MarketComment;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketCommentRepository extends JpaRepository<MarketComment, Long> {

}