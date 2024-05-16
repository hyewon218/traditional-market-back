package com.market.domain.member.service;

import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.config.MyUserDetails;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;


    // 회원 생성
    @Override
    public Member createMember(MemberRequestDto memberRequestDto){
        return memberRepository.save(memberRequestDto.toEntity(passwordEncoder));
    }

    // 전체 회원 조회
    @Override
    public List<Member> findAll() {
        return memberRepository.findAll();
    }

    // 특정 회원 조회
    @Override
    public Member findById(long memberNo) {
        return memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디 조회 실패 : " + memberNo));
    }

    // 회원 수정
    @Override
    @Transactional
    public Member update(long memberNo, MemberRequestDto requestDto) {
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디 조회 실패 : " + memberNo));

        member.update(requestDto.getMemberId(), passwordEncoder.encode(requestDto.getMemberPw()));
        return member;
    }

    // 회원 삭제
    @Override
    public void deleteMember(long memberNo) {
        memberRepository.deleteById(memberNo);
    }


}
