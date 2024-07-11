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
    Boolean existsByMemberEmail(String memberEmail);
    Member findByMemberIdAndMemberPw(String memberId, String memberPw);
    Optional<Member> findByRole(Role role);

    // 아이디 찾기(닉네임과 이메일 이용)
    Member findByMemberNicknameAndMemberEmail(String memberNickname, String memberEmail);

    // 비밀번호 찾기(아이디와 이메일 이용)
    Member findByMemberIdAndMemberEmail(String memberId, String memberEmail);
}
