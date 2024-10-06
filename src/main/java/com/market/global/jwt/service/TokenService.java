package com.market.global.jwt.service;

import com.market.domain.member.entity.Member;
import com.market.domain.member.service.MemberServiceImpl;
import com.market.global.jwt.config.TokenProvider;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Service
@RequiredArgsConstructor
// 사용 안함
public class TokenService {

    private final TokenProvider tokenProvider;
    private final RefreshTokenService refreshTokenService;
    private final MemberServiceImpl memberService;

    // tokenProvider로 옮김
    public String createNewAccessToken(String refreshToken, HttpServletRequest request,
        HttpServletResponse response) throws Exception {
        if (!tokenProvider.validRefreshToken(refreshToken, request, response)) {
            throw new IllegalArgumentException("리프레시토큰이 유효하지않습니다.");
        }
        Long memberNo = refreshTokenService.findByRefreshToken(refreshToken).getMemberNo();
        Member member = memberService.findById(memberNo);

        return tokenProvider.generateToken(member, Duration.ofHours(2));
    }
    
    // 액세스토큰 쿠키에서 가져오기
    public String getAccessTokenFromCookie(HttpServletRequest request) {
        return tokenProvider.getTokenFromCookie(request);
    }

    // 리프레시토큰 쿠키에서 가져오기
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        return tokenProvider.getRefreshTokenFromCookie(request);
    }
}
