package com.market.domain.member.repository;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.global.security.oauth2.ProviderType;
import io.lettuce.core.dynamic.annotation.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
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

    Page<Member> findAllByRole(Role role, Pageable pageable);

    List<Member> findAllByRole(Role role);

    // 아이디 찾기(닉네임과 이메일 이용)
    Member findByMemberNicknameAndMemberEmail(String memberNickname, String memberEmail);

    // 비밀번호 찾기(아이디와 이메일 이용)
    Member findByMemberIdAndMemberEmail(String memberId, String memberEmail);

    Long countByProviderType(ProviderType type);

    // 30일 이전, isWarning(제재 여부)이 true인 회원 찾기
    List<Member> findByIsWarningAndWarningStartDateBefore(boolean isWarning,
        LocalDateTime thirtyDaysAgo);

    // 모든 회원의 reportMember 리스트를 빈 리스트로 초기화
    @Modifying
    @Query("UPDATE Member m SET m.reportMember = :reportMember")
    void clearAllReportMemberLists(@Param("reportMember") List<Long> reportMember);
}
