package com.market.domain.member.repository;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    Optional<Member> findByMemberId(String memberId);
    Optional<Member> findByMemberEmail(String memberEmail);
    Boolean existsByMemberNickname(String memberNickname);
    Boolean existsByMemberId(String memberId);
    Member findByMemberIdAndMemberPw(String memberId, String memberPw);
    Optional<Member> findByRole(Role role);
}
