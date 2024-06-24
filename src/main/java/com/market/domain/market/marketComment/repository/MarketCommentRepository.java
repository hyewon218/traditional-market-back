package com.market.domain.market.marketComment.repository;

import com.market.domain.market.marketComment.entity.MarketComment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MarketCommentRepository extends JpaRepository<MarketComment, Long> {
    Page<MarketComment> findAllByMarket_NoOrderByCreateTimeDesc(Long no, Pageable pageable);
}