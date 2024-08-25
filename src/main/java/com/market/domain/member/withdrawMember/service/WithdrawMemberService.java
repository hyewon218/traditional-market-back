package com.market.domain.member.withdrawMember.service;

import com.market.domain.member.entity.Member;
import com.market.domain.member.withdrawMember.dto.WithdrawMemberResponseDto;
import com.market.domain.member.withdrawMember.entity.WithdrawMember;
import com.market.domain.member.withdrawMember.repository.WithdrawMemberSearchCond;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
public interface WithdrawMemberService {

    /**
     * 탈퇴 회원 생성
     *
     * @param member : 생성 시 필요한 탈퇴 회원 정보
     * @param ipAddr : 탈퇴 회원의 ip 주소
     * @return : 탈퇴 정보
     */
    WithdrawMemberResponseDto createWithdrawMember(Member member, String ipAddr);

    /**
     * 전체 조회
     *
     * @param member : 관리자인지 검증할 회원 정보
     * @return : 전체 목록
     */
    Page<WithdrawMemberResponseDto> getAllWithdrawMembers(Member member, Pageable pageable);

    /**
     * 키워드 검색 탈퇴회원 목록 조회
     *
     * @param cond 조건
     * @return 검색한 키워드가 있는 탈퇴회원 목록 조회
     */
    Page<WithdrawMemberResponseDto> searchWithdrawMembers(WithdrawMemberSearchCond cond, Pageable pageable);

    /**
     * 단건 조회
     *
     * @param member : 관리자인지 검증할 회원 정보
     * @param withdrawMemberNo : 조회할 탈퇴 회원 정보
     * @return : 탈퇴 회원 정보
     */
    WithdrawMemberResponseDto getWithdrawMember(Member member, Long withdrawMemberNo);


    /**
     * 삭제
     *
     * @param member : 관리자인지 검증할 회원 정보
     * @param withdrawMemberNo : 삭제할 탈퇴 회원 정보
     */
    void deleteWithdrawMember(Member member, Long withdrawMemberNo);

    /**
     * 전체 삭제
     *
     * @param member : 관리자인지 검증할 회원 정보
     */
    void deleteAll(Member member);

    /**
     * 관리자 권한 검증
     *
     * @param member : 관리자인지 검증할 회원 정보
     */
    void validationAdmin(Member member);

    /**
     * 탈퇴 회원 찾기
     *
     * @param withdrawMemberNo : 찾을 탈퇴 회원 정보
     * @return : 탈퇴 회원
     */
    WithdrawMember findById(Long withdrawMemberNo);

    /**
     * 회원가입 시 탈퇴 회원 검증 (id 이용)
     *
     * @param memberId : 찾을 탈퇴 회원 정보
     */
    boolean existsMemberId(String memberId);

    /**
     * 회원가입 시 탈퇴 회원 검증 (email 이용)
     *
     * @param memberEmail : 찾을 탈퇴 회원 정보
     */
    boolean existsMemberEmail(String memberEmail);

    /**
     * 회원가입 시 탈퇴 회원 검증 (ipAddr 이용)
     *
     * @param ipAddr : 찾을 탈퇴 회원 정보
     */
    boolean existsIpAddr(String ipAddr);
}
