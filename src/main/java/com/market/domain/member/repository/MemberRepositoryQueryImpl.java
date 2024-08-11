package com.market.domain.member.repository;

import com.market.domain.member.entity.Member;
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

@Repository
@RequiredArgsConstructor
public class MemberRepositoryQueryImpl implements MemberRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Member> searchMembers(MemberSearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(member)
            .from(member)
            .where(
                contentContains(cond.getKeyword())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(member)
            .where(contentContains(cond.getKeyword()))
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);

    }

    private static BooleanExpression contentContains(String keyword) {
        return Objects.nonNull(keyword) ? member.memberId.contains(keyword) : null;
    }
}
