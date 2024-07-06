package com.market.domain.item.repository;

import static com.market.domain.item.entity.QItem.item;

import com.market.domain.item.entity.Item;
import com.market.domain.market.entity.QMarket;
import com.market.domain.shop.entity.QShop;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class ItemRepositoryQueryImpl implements ItemRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Item> searchItems(ItemSearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(item)
            .from(item)
            .where(
                contentContains(cond.getKeyword())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(item)
            .where(contentContains(cond.getKeyword()))
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);

    }

    private static BooleanExpression contentContains(String keyword) {
        return Objects.nonNull(keyword) ? item.itemName.contains(keyword) : null;
    }

    @Override
    public List<Item> searchRankingFiveItems(ItemSearchCond cond) {
        var query = jpaQueryFactory.select(item)
            .from(item)
            .where(
                contentContains(cond.getKeyword())
            )
            .orderBy(item.price.asc())
            .limit(5); // 5위까지

        return query.fetch();
    }

    // 상점 고유번호와 상품 이름을 이용해 상품 조회
    @Override
    public List<Item> searchItemsByShopNoAndItemName(Long marketNo, String itemName) {
        QShop shop = QShop.shop;
        QMarket market = QMarket.market;

        var query = jpaQueryFactory.selectFrom(item)
                .join(item.shop, shop).fetchJoin()
                .join(shop.market, market).fetchJoin()
                .where(market.no.eq(marketNo).and(item.itemName.eq(itemName)))
                .orderBy(item.price.asc())
                .limit(5);

        System.out.println(query.toString()); // 생성된 SQL 쿼리 출력

        List<Item> items = query.fetch();
        return items;
    }
}
