package com.market.domain.notice.repository;

import com.market.domain.member.entity.Member;
import com.market.domain.notice.entity.Notice;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static com.market.domain.member.entity.QMember.member;
import static com.market.domain.notice.entity.QNotice.notice;

@Repository
@RequiredArgsConstructor
public class NoticeRepositoryQueryImpl implements NoticeRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Notice> searchNotices(NoticeSearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(notice)
            .from(notice)
            .where(
                contentContains(cond.getKeyword())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(notice)
            .where(contentContains(cond.getKeyword()))
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);

    }

    private static BooleanExpression contentContains(String keyword) {
        return Objects.nonNull(keyword) ? notice.noticeTitle.contains(keyword) : null;
    }
}
