package com.market.domain.member.service;

import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.jwt.repository.RefreshTokenRepository;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationConfiguration authenticationConfiguration;
    private final TokenProvider tokenProvider;

    // 로그인
//    @Override
//    public void logIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
//                                MemberRequestDto request) throws Exception {
//        Authentication authentication = authenticationConfiguration.getAuthenticationManager()
//                .authenticate(
//                        new UsernamePasswordAuthenticationToken(
//                                request.getMemberId(),
//                                request.getMemberPw(),
//                                null
//                        )
//                );
//
//        SecurityContextHolder.getContext().setAuthentication(authentication);
//
//        Member member = ((UserDetailsImpl) authentication.getPrincipal()).getMember();
//
//        // access 토큰 생성
//        String accessToken = tokenProvider.generateToken(member, TokenProvider.ACCESS_TOKEN_DURATION);
//
//        // refresh 토큰 생성
//        RefreshToken findRefreshToken = refreshTokenRepository.findByMemberNo(member.getMemberNo());
//            if (findRefreshToken == null || !tokenProvider.validRefreshToken(findRefreshToken)) {
//            RefreshToken refreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
//            log.info("refresh 토큰이 생성되었습니다 : " + refreshToken)
//        }
//    }

    // 로그인, access 토큰, refresh 토큰 모두 ApiLoginSuccessHandler에서 생성(포스트맨에서 해당 객체 보기 위해)
    @Override
    public Authentication logIn(HttpServletRequest httpRequest, HttpServletResponse httpResponse,
                      MemberRequestDto request) throws Exception {
        Authentication authentication = authenticationConfiguration.getAuthenticationManager()
                .authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getMemberId(),
                                request.getMemberPw(),
                                null
                        )
                );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        Member member = ((UserDetailsImpl) authentication.getPrincipal()).getMember();

        return authentication;
    }

    // 로그아웃


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

    // 회원 삭제(해당 회원의 refresh 토큰도 함께 삭제)
    @Override
    public void deleteMember(long memberNo) {
        RefreshToken refreshToken = refreshTokenRepository.findByMemberNo(memberNo);
        memberRepository.deleteById(memberNo);
        refreshTokenRepository.delete(refreshToken);
    }


}
