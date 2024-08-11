package com.market.domain.shop.repository;

import static com.market.domain.shop.entity.QShop.shop;

import com.market.domain.shop.entity.Shop;
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
public class ShopRepositoryQueryImpl implements ShopRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Shop> searchShops(ShopSearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(shop)
            .from(shop)
            .where(
                contentContains(cond.getKeyword())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(shop)
            .where(contentContains(cond.getKeyword()))
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);

    }

    private static BooleanExpression contentContains(String keyword) {
        return Objects.nonNull(keyword) ? shop.shopName.contains(keyword) : null;
    }
}
