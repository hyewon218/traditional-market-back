package com.market.domain.order.repository;

import static com.market.domain.item.entity.QItem.item;
import static com.market.domain.member.entity.QMember.member;
import static com.market.domain.order.entity.QOrder.order;
import static com.market.domain.orderItem.entity.QOrderItem.orderItem;
import static com.market.domain.shop.entity.QShop.shop;

import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberSearchCond;
import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.entity.Order;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;

import java.util.Objects;
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
//////////////////////////////////// 본인 주문 목록 조회 /////////////////////////////////////////////
    @Override // CANCEL, ORDER 제외 전체 주문 목록 (본인)
    public Page<Order> findOrders(Long memberNo, Pageable pageable) {

        var result = jpaQueryFactory.selectFrom(order)
            .where(order.member.memberNo.eq(memberNo)
                .and(order.orderStatus.ne(OrderStatus.CANCEL))
                .and(order.orderStatus.ne(OrderStatus.ORDER)))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long totalSize = Optional.ofNullable(
            jpaQueryFactory.select(Wildcard.count) // select count(*)
                .from(order)
                .where(order.member.memberNo.eq(memberNo)
                    .and(order.orderStatus.ne(OrderStatus.CANCEL))
                    .and(order.orderStatus.ne(OrderStatus.ORDER)))
                .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(result, pageable, () -> totalSize);
    }

    @Override // CANCEL 주문 목록 (본인)
    public Page<Order> findCancelOrders(Long memberNo, Pageable pageable) {

        var result = jpaQueryFactory.selectFrom(order)
            .where(order.member.memberNo.eq(memberNo).and(order.orderStatus.eq(OrderStatus.CANCEL)))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .fetch();

        long totalSize = Optional.ofNullable(
            jpaQueryFactory.select(Wildcard.count) // select count(*)
                .from(order)
                .where(order.member.memberNo.eq(memberNo).and(order.orderStatus.eq(OrderStatus.CANCEL)))
                .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(result, pageable, () -> totalSize);
    }
////////////////////////////////////// 관리자 주문 목록 조회 ///////////////////////////////////////
    @Override // (CANCEL, ORDER 제외) 관리자가 전체 주문 목록 조회
    public Page<Order> findOrdersByAdminExcludingCanceled(Pageable pageable) {
        var query = jpaQueryFactory.select(order)
            .from(order)
            .where(order.orderStatus.ne(OrderStatus.CANCEL)
                .and(order.orderStatus.ne(OrderStatus.ORDER)))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(order.orderDate.desc());

        var posts = query.fetch();

        long totalSize = Optional.ofNullable(
            jpaQueryFactory.select(Wildcard.count)
                .from(order)
                .where(order.orderStatus.ne(OrderStatus.CANCEL)
                    .and(order.orderStatus.ne(OrderStatus.ORDER)))
                .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);
    }

    @Override // 관리자가 주문 상태별 조회
    public Page<Order> findOrdersByAdminAndOrderStatus(OrderStatus orderStatus, Pageable pageable) {

        var query = jpaQueryFactory.select(order)
            .from(order)
            .where(order.orderStatus.eq(orderStatus))
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize())
            .orderBy(order.orderDate.desc());

        var posts = query.fetch();

        long totalSize = Optional.ofNullable(
            jpaQueryFactory.select(Wildcard.count)
                .from(order)
                .where(order.orderStatus.eq(orderStatus))
                .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);
    }
///////////////////////////////////////// 판매자 주문 목록 조회 ///////////////////////////////////////
    @Override // (CANCEL, ORDER 제외) 판매자가 자신이 소유한 상점의 상품들에 대한 주문 목록 조회 (판매자만 가능)
    public Page<Order> findOrdersBySellerExcludingCanceled(Long sellerNo, Pageable pageable) {
        var query = jpaQueryFactory.select(order)
            .from(order)
            .join(order.orderItemList, orderItem)
            .join(orderItem.item, item)
            .join(item.shop, shop)
            .where(shop.seller.memberNo.eq(sellerNo)
                .and(order.orderStatus.ne(OrderStatus.CANCEL))
                .and(order.orderStatus.ne(OrderStatus.ORDER)))
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
                .where(shop.seller.memberNo.eq(sellerNo)
                    .and(order.orderStatus.ne(OrderStatus.CANCEL))
                    .and(order.orderStatus.ne(OrderStatus.ORDER)))
                .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);
    }

    @Override // 판매자가 자신이 소유한 상점의 상품들에 대한 주문 상태별 조회 (판매자만 가능)
    public Page<Order> findOrdersBySellerAndOrderStatus(Long sellerNo, OrderStatus orderStatus,
        Pageable pageable) {

        var query = jpaQueryFactory.select(order)
            .from(order)
            .join(order.orderItemList, orderItem)
            .join(orderItem.item, item)
            .join(item.shop, shop)
            .where(shop.seller.memberNo.eq(sellerNo).and(order.orderStatus.eq(orderStatus)))
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
                .where(shop.seller.memberNo.eq(sellerNo).and(order.orderStatus.eq(orderStatus)))
                .fetchOne()
        ).orElse(0L);

        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);
    }

///////////////////////////////////////// 주문 검색 //////////////////////////////////////////////
    @Override // 관리자 주문 관리 페이지 내 주문 검색 (랜덤주문번호, 회원 아이디별)
    public Page<Order> searchOrders(OrderSearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(order)
            .from(order)
            .where(
                contentContains(cond.getKeyword(), cond.getType())
                    .and(order.orderStatus.ne(OrderStatus.ORDER))
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(order)
            .where(
                contentContains(cond.getKeyword(), cond.getType())
                    .and(order.orderStatus.ne(OrderStatus.ORDER))
            )
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);
    }

    @Override // 판매자 주문 관리 페이지 내 주문 검색 (랜덤주문번호, 회원 아이디별)
    public Page<Order> searchOrdersSeller(Long sellerNo, OrderSearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(order)
            .from(order)
            .join(order.orderItemList, orderItem)
            .join(orderItem.item, item)
            .join(item.shop, shop)
            .where(
                shop.seller.memberNo.eq(sellerNo)
                    .and(contentContains(cond.getKeyword(), cond.getType()))
                    .and(order.orderStatus.ne(OrderStatus.ORDER))
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count)
            .from(order)
            .join(order.orderItemList, orderItem)
            .join(orderItem.item, item)
            .join(item.shop, shop)
            .where(
                shop.seller.memberNo.eq(sellerNo)
                    .and(contentContains(cond.getKeyword(), cond.getType()))
                    .and(order.orderStatus.ne(OrderStatus.ORDER))
            )
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);
    }

    private static BooleanExpression contentContains(String keyword, String type) {
        if (Objects.isNull(keyword) || keyword.isEmpty()) {
            return null;
        }
        return switch (type) {
            case "randomOrderNo" -> order.randomOrderNo.contains(keyword);
            default -> order.member.memberId.contains(keyword);
        };
    }

}
