package com.market.domain.member.service;

import com.market.domain.member.dto.MemberRequestDto;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.repository.RefreshTokenRepository;
import com.market.global.redis.RedisUtils;
import com.market.global.security.CookieUtil;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
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
    private final RedisUtils redisUtils;

    // 회원 생성
    @Override
    public Member createMember(MemberRequestDto memberRequestDto){
        return memberRepository.save(memberRequestDto.toEntity(passwordEncoder));
    }

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
        try {
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
        } catch (AuthenticationException e) {
            log.info("아이디 또는 패스워드가 틀렸습니다");
            throw e;
        }
    }

    // 로그아웃
    @Override
    public void logOut(HttpServletRequest httpRequest, HttpServletResponse httpResponse) {

        String authorizationHeader = httpRequest.getHeader(TokenProvider.HEADER_AUTHORIZATION);
        String accessToken = tokenProvider.getAccessToken(authorizationHeader);
        String memberId = tokenProvider.getMemberId(accessToken);
        // 사용자 아이디 이용해서 리프레시 토큰 삭제
        redisUtils.deleteValues(memberId);
        redisUtils.setBlackList(memberId, "logout", tokenProvider.getExpiration(accessToken));
        // 쿠키 삭제
        CookieUtil.deleteCookie(httpRequest, httpResponse, TokenProvider.HEADER_AUTHORIZATION);
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

        if (memberRepository.existsByMemberNickname(requestDto.getMemberNickname())) {
            log.info("requestDto.getMemberNickname : " + requestDto.getMemberNickname());
            log.info("member.getMemberNickname : " + member.getMemberNickname());
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다");
        }
        member.update(requestDto.getMemberNickname(), passwordEncoder.encode(requestDto.getMemberPw()));
        return member;
    }

    // 회원 삭제(해당 회원의 refresh 토큰도 함께 삭제)
    @Override
    public void deleteMember(long memberNo, String memberId, HttpServletRequest httpRequest, HttpServletResponse httpResponse) {
        memberRepository.deleteById(memberNo);
        redisUtils.deleteValues(memberId);
        CookieUtil.deleteCookie(httpRequest, httpResponse, TokenProvider.HEADER_AUTHORIZATION);
    }

    ///////////////////////////////////////////////
    // OAuth2 인증 성공 후 추가 정보 수정 실행(memberNickname)
    @Override
    public Member updateNickname(String memberId, MemberRequestDto memberRequestDto) {
        Member member = memberRepository.findByMemberId(memberId)
                .orElseThrow(() -> new IllegalArgumentException("해당 아이디 조회 실패 : " + memberId));

        if (memberRepository.existsByMemberNickname(memberRequestDto.getMemberNickname())) {
            log.info("requestDto.getMemberNickname : " + memberRequestDto.getMemberNickname());
            log.info("member.getMemberNickname : " + member.getMemberNickname());
            throw new IllegalArgumentException("이미 존재하는 닉네임입니다");
        }
        member.updateNickname(memberRequestDto.getMemberNickname());
        return member;
    }
    ///////////////////////////////////////////////
}
