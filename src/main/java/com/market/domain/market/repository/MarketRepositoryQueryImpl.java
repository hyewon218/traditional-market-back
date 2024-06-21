package com.market.domain.market.repository;

import static com.market.domain.market.entity.QMarket.market;

import com.market.domain.market.entity.Market;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MarketRepositoryQueryImpl implements MarketRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Market> searchMarkets(MarketSearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(market)
            .from(market)
            .where(
                contentContains(cond.getKeyword())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(market)
            .where(contentContains(cond.getKeyword()))
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);

    }

    private static BooleanExpression contentContains(String keyword) {
        return Objects.nonNull(keyword) ? market.marketName.contains(keyword) : null;
    }
}
