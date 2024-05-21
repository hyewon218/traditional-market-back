package com.market.global.jwt.service;

import com.market.global.jwt.config.TokenProvider;
import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberServiceImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberServiceImpl memberService;

    public String createNewAccessToken(String refreshToken) {
        // 토큰 유효성 검사에 실패하면 예외 발생
        if(!tokenProvider.validToken(refreshToken)) {
            // refresh 토큰 발급 로직 작성하기(로운 refresh 토큰 발급 시, 해당 회원 번호 가져오기)
            throw new IllegalArgumentException("토큰이 유효하지않습니다.");
        }
        Long memberNo = refreshTokenService.findByRefreshToken(refreshToken).getMemberNo();
        Member member = memberService.findById(memberNo);

//        return tokenProvider.generateToken(member, Duration.ofHours(2));
        return tokenProvider.generateToken(member, Duration.ofHours(2));
    }
}
