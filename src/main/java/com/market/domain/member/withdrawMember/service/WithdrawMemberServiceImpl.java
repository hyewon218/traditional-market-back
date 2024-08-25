package com.market.domain.member.withdrawMember.service;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.withdrawMember.dto.WithdrawMemberResponseDto;
import com.market.domain.member.withdrawMember.entity.WithdrawMember;
import com.market.domain.member.withdrawMember.repository.WithdrawMemberRepository;
import com.market.domain.member.withdrawMember.repository.WithdrawMemberRepositoryQuery;
import com.market.domain.member.withdrawMember.repository.WithdrawMemberSearchCond;
import com.market.global.exception.BusinessException;
import com.market.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class WithdrawMemberServiceImpl implements WithdrawMemberService {

    private final WithdrawMemberRepository withdrawMemberRepository;
    private final WithdrawMemberRepositoryQuery withdrawMemberRepositoryQuery;

    // 탈퇴 회원 생성
    @Override
    @Transactional
    public WithdrawMemberResponseDto createWithdrawMember(Member member, String ipAddr) {
        return WithdrawMemberResponseDto.of(
            withdrawMemberRepository.save(WithdrawMember.toEntity(member, ipAddr)));
    }

    // 전체 조회
    @Override
    @Transactional(readOnly = true)
    public Page<WithdrawMemberResponseDto> getAllWithdrawMembers(Member member, Pageable pageable) {
        validationAdmin(member);
        Page<WithdrawMember> withdrawMembers = withdrawMemberRepository.findAll(pageable);
        return withdrawMembers.map(WithdrawMemberResponseDto::of);
    }

    // 키워드 검색 탈퇴회원 목록 조회
    @Override
    @Transactional(readOnly = true)
    public Page<WithdrawMemberResponseDto> searchWithdrawMembers(WithdrawMemberSearchCond cond,
        Pageable pageable) {
        return withdrawMemberRepositoryQuery.searchWithdrawMembers(cond, pageable)
            .map(WithdrawMemberResponseDto::of);
    }

    // 단건 조회
    @Override
    @Transactional(readOnly = true)
    public WithdrawMemberResponseDto getWithdrawMember(Member member, Long withdrawMemberNo) {
        validationAdmin(member);
        return WithdrawMemberResponseDto.of(findById(withdrawMemberNo));
    }

    // 삭제
    @Override
    @Transactional
    public void deleteWithdrawMember(Member member, Long withdrawMemberNo) {
        validationAdmin(member);
        withdrawMemberRepository.delete(findById(withdrawMemberNo));
    }

    // 전체 삭제
    @Override
    @Transactional
    public void deleteAll(Member member) {
        validationAdmin(member);
        withdrawMemberRepository.deleteAll();
    }

    // 매일 자정마다 탈퇴회원 30일 지났는지 확인하고 30일 지났으면 DB에서 자동 삭제
    @Scheduled(cron = "0 0 0 * * ?") // 매일 자정에 실행
    public void deleteWithdrawMember() {
        log.info("탈퇴회원 삭제 스케줄러 실행");

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime oneMinutesAgo = now.minusMinutes(1); // 30일로 변경하기

        // 탈퇴회원 중 탈퇴한 날짜가 30일 이전인 사용자 찾아서 삭제
        withdrawMemberRepository.findByWithdrawDateBefore(oneMinutesAgo)
            .forEach(withdrawMemberRepository::delete);
    }

    // 관리자 권한 검증
    @Override
    public void validationAdmin(Member member) {
        if (member.getRole() != Role.ADMIN) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    // 탈퇴 회원 찾기 (no 이용)
    @Override
    @Transactional(readOnly = true)
    public WithdrawMember findById(Long withdrawMemberNo) {
        return withdrawMemberRepository.findById(withdrawMemberNo)
            .orElseThrow(() -> new IllegalArgumentException("일치하는 탈퇴 회원이 없습니다"));
    }

    // 회원가입 시 탈퇴 회원 검증 (id 이용)
    @Override
    public boolean existsMemberId(String memberId) {
        return withdrawMemberRepository.existsByWithdrawMemberId(memberId);
    }

    // 회원가입 시 탈퇴 회원 검증 (email 이용)
    @Override
    public boolean existsMemberEmail(String memberEmail) {
        return withdrawMemberRepository.existsByWithdrawMemberEmail(memberEmail);
    }

    // 회원가입 시 탈퇴 회원 검증 (ipAddr 이용)
    @Override
    public boolean existsIpAddr(String ipAddr) {
        return withdrawMemberRepository.existsByWithdrawIpAddr(ipAddr);
    }
}
