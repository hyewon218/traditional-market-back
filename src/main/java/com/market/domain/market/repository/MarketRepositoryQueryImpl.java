package com.market.domain.market.repository;

import static com.market.domain.image.entity.QImage.image;
import static com.market.domain.market.entity.QMarket.market;
import static com.market.domain.market.marketLike.entity.QMarketLike.marketLike;

import com.market.domain.image.dto.ImageResponseDto;
import com.market.domain.market.dto.MarketLikeResponseDto;
import com.market.domain.market.entity.Market;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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

        var markets = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(market)
            .where(contentContains(cond.getKeyword()))
            .fetch().get(0);
        return PageableExecutionUtils.getPage(markets, pageable, () -> totalSize);

    }

    @Override
    public Page<MarketLikeResponseDto> findMarketsSortedByLikes(Pageable pageable) {

        // 첫 번째 쿼리: Market 정보와 좋아요 수를 가져오기
        List<Tuple> results = jpaQueryFactory
            .select(
                market.no,
                market.marketName,
                marketLike.count().longValue() // 좋아요 수
            )
            .from(market)
            .leftJoin(market.marketLikeList, marketLike)
            .groupBy(market.no, market.marketName)
            .orderBy(marketLike.count().desc()) // likes count 로 정렬
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        // 결과를 DTO 로 변환
        List<MarketLikeResponseDto> marketLikeResponseDtos = new ArrayList<>();
        for (Tuple tuple : results) {
            Long marketNo = tuple.get(0, Long.class);
            String marketName = tuple.get(1, String.class);
            Long likes = tuple.get(2, Long.class);

            // 두 번째 쿼리: 해당 Market 의 이미지 URL 리스트를 가져오기
            List<String> imageUrls = jpaQueryFactory
                .select(image.imageUrl)
                .from(image)
                .where(image.market.no.eq(marketNo))
                .fetch();

            // ImageResponseDto 로 변환
            List<ImageResponseDto> imageResponseDtos = imageUrls.stream()
                .map(url -> ImageResponseDto.builder().imageUrl(url).build())
                .collect(Collectors.toList());

            marketLikeResponseDtos.add(
                new MarketLikeResponseDto(marketNo, marketName, likes, imageResponseDtos));
        }

        long totalSize = jpaQueryFactory
            .select(market.no.countDistinct())
            .from(market)
            .leftJoin(market.marketLikeList, marketLike)
            .fetchOne();

        return PageableExecutionUtils.getPage(marketLikeResponseDtos, pageable, () -> totalSize);
    }

    private static BooleanExpression contentContains(String keyword) {
        return Objects.nonNull(keyword) ? market.marketName.contains(keyword) : null;
    }
}
