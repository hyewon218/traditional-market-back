package com.market.domain.member.withdrawMember.repository;

import com.market.domain.member.withdrawMember.entity.WithdrawMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface WithdrawMemberRepository extends JpaRepository<WithdrawMember, Long> {

    boolean existsByWithdrawMemberId(String memberId);

    boolean existsByWithdrawMemberEmail(String memberEmail);

    boolean existsByWithdrawIpAddr(String ipAddr);

    List<WithdrawMember> findByWithdrawDateBefore(LocalDateTime withdrawDate);
}
