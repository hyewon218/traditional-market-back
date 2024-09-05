package com.market.domain.item.repository;

import static com.market.domain.item.entity.QItem.item;
import static com.market.domain.market.entity.QMarket.market;
import static com.market.domain.shop.entity.QShop.shop;

import com.market.domain.item.dto.ItemTop5ResponseDto;
import com.market.domain.item.entity.Item;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Slf4j
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
    public List<ItemTop5ResponseDto> searchItemsByMarketNoAndItemName(Long marketNo, String itemName) {

        var query = jpaQueryFactory.select(
                item.no,
                item.itemName,
                item.price,
                shop.no,
                shop.shopName,
                market.marketName
            )
            .from(item)
            .join(item.shop, shop)
            .join(shop.market, market)
            .where(market.no.eq(marketNo).and(item.itemName.eq(itemName)))
            .orderBy(item.price.asc())
            .limit(5);

        // SQL 쿼리 로그 출력
        log.debug("Executing query: {}", query);

        List<Tuple> results = query.fetch(); // 쿼리 실행 및 결과 가져오기

        // Tuple 을 DTO 로 변환
        return results.stream()
            .map(tuple -> ItemTop5ResponseDto.builder()
                .itemNo(tuple.get(item.no))
                .price(tuple.get(item.price))
                .shopNo(tuple.get(shop.no))
                .shopName(tuple.get(shop.shopName))
                .build())
            .collect(Collectors.toList());
    }
}
