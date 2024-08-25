package com.market.domain.member.withdrawMember.repository;

import com.market.domain.member.withdrawMember.entity.WithdrawMember;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface WithdrawMemberRepositoryQuery {

    Page<WithdrawMember> searchWithdrawMembers(WithdrawMemberSearchCond cond, Pageable pageable);
}