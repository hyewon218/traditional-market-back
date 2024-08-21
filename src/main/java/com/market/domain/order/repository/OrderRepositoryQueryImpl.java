package com.market.domain.order.repository;

import static com.market.domain.item.entity.QItem.item;
import static com.market.domain.order.entity.QOrder.order;
import static com.market.domain.orderItem.entity.QOrderItem.orderItem;
import static com.market.domain.shop.entity.QShop.shop;

import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.entity.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class OrderRepositoryQueryImpl implements OrderRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override // 삭제될 이전 ORDER 주문
    public Order findOrder(Long memberNo, OrderStatus ORDER) {

        var query = jpaQueryFactory.select(order)
            .from(order)
            .where(order.member.memberNo.eq(memberNo).and(order.orderStatus.eq(ORDER)))
            .orderBy(order.orderDate.desc());

        return query.fetchOne();
    }

    @Override
    public Optional<Order> findLatestOrder(Long memberNo, OrderStatus orderStatus) {
        return findLatestOrderByStatus(memberNo, orderStatus);
    }

    private Optional<Order> findLatestOrderByStatus(Long memberNo, OrderStatus orderStatus) {

        var query = jpaQueryFactory.select(order)
            .from(order)
            .where(order.member.memberNo.eq(memberNo)
                .and(order.orderStatus.eq(orderStatus)))
            .orderBy(order.orderDate.desc())
            .limit(1); // 가장 최근 주문

        return Optional.ofNullable(query.fetchOne());
    }

    @Override // 결제 완료 COMPLETE 된 주문 목록
    public Page<Order> findCompleteOrders(Long memberNo, OrderStatus COMPLETE,
        Pageable pageable) {

        var result = jpaQueryFactory.select(order)
            .from(order)
            .where(order.member.memberNo.eq(memberNo).and(order.orderStatus.eq(COMPLETE)))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long totalSize = Optional.ofNullable(
            jpaQueryFactory.select(Wildcard.count) //select count(*)
                .from(order)
                .where(order.member.memberNo.eq(memberNo).and(order.orderStatus.eq(COMPLETE)))
                .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(result, pageable, () -> totalSize);
    }

    @Override // (CANCEL 제외) 판매자가 자신이 소유한 상점의 상품들에 대한 주문 목록 조회 (판매자만 가능)
    public Page<Order> findOrdersBySellerExcludingCanceled(Long memberNo, OrderStatus CANCEL,
        Pageable pageable) {
        return findOrdersBySeller(memberNo, order.orderStatus.ne(CANCEL), pageable);
    }

    @Override // 판매자가 자신이 소유한 상점의 상품들에 대한 주문 상태별 조회 (판매자만 가능)
    public Page<Order> findOrdersBySellerAndOrderStatus(Long memberNo, OrderStatus orderStatus,
        Pageable pageable) {
        return findOrdersBySeller(memberNo, order.orderStatus.eq(orderStatus), pageable);
    }

    private Page<Order> findOrdersBySeller(Long memberNo, BooleanExpression orderStatusCondition,
        Pageable pageable) {

        var query = jpaQueryFactory.select(order)
            .from(order)
            .join(order.orderItemList, orderItem)
            .join(orderItem.item, item)
            .join(item.shop, shop)
            .where(shop.seller.memberNo.eq(memberNo).and(orderStatusCondition))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(order.orderDate.desc());

        var posts = query.fetch();

        long totalSize = Optional.ofNullable(
            jpaQueryFactory.select(Wildcard.count)
                .from(order)
                .join(order.orderItemList, orderItem)
                .join(orderItem.item, item)
                .join(item.shop, shop)
                .where(shop.seller.memberNo.eq(memberNo).and(orderStatusCondition))
                .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);
    }
}
