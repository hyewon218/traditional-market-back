package com.market.domain.inquiry.repository;

import com.market.domain.inquiry.entity.Inquiry;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Wildcard;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

import java.util.Objects;

import static com.market.domain.inquiry.entity.QInquiry.inquiry;

@Repository
@RequiredArgsConstructor
public class InquiryRepositoryQueryImpl implements InquiryRepositoryQuery {

    private final JPAQueryFactory jpaQueryFactory;

    @Override
    public Page<Inquiry> searchInquiries(InquirySearchCond cond, Pageable pageable) {
        var query = jpaQueryFactory.select(inquiry)
            .from(inquiry)
            .where(
                contentContains(cond.getKeyword(), cond.getType())
            )
            .offset(pageable.getOffset())
            .limit(pageable.getPageSize());

        var posts = query.fetch();

        long totalSize = jpaQueryFactory.select(Wildcard.count) //select count(*)
            .from(inquiry)
            .where(contentContains(cond.getKeyword(), cond.getType()))
            .fetch().get(0);
        return PageableExecutionUtils.getPage(posts, pageable, () -> totalSize);

    }

    private static BooleanExpression contentContains(String keyword, String type) {
        if (Objects.isNull(keyword) || keyword.isEmpty()) {
            return null;
        }
        return switch (type) {
            case "id" -> inquiry.inquiryWriter.contains(keyword);
            default -> inquiry.inquiryTitle.contains(keyword);
        };
    }
}
