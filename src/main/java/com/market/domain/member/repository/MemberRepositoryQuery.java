package com.market.domain.member.repository;

import com.market.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MemberRepositoryQuery {

    Page<Member> searchMembers(MemberSearchCond cond, Pageable pageable);
}