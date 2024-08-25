package com.market.domain.member.withdrawMember.repository;

import com.market.domain.member.withdrawMember.entity.WithdrawMember;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static com.market.domain.member.withdrawMember.entity.QWithdrawMember.withdrawMember;

@Repository
@RequiredArgsConstructor
public class WithdrawMemberRepositoryQueryImpl implements WithdrawMemberRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<WithdrawMember> searchWithdrawMembers(WithdrawMemberSearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(withdrawMember)
            .from(withdrawMember)
            .where(
                contentContains(cond.getKeyword(), cond.getType())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(withdrawMember)
            .where(contentContains(cond.getKeyword(), cond.getType()))
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);
    }

    private static BooleanExpression contentContains(String keyword, String type) {
        if (Objects.isNull(keyword) || keyword.isEmpty()) {
            return null;
        }
        return switch (type) {
            case "withdrawMemberEmail" -> withdrawMember.withdrawMemberEmail.contains(keyword);
            default -> withdrawMember.withdrawMemberId.contains(keyword);
        };
    }
}

