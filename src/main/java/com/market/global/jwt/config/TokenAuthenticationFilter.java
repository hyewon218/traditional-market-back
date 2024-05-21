package com.market.global.jwt.config;

import com.market.domain.member.entity.Member;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.jwt.repository.RefreshTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {

        // 요청 헤더와 Authorization 키의 값
        String authorizationHeader = request.getHeader(TokenProvider.HEADER_AUTHORIZATION);

        // 가져온 값에서 접두사 제거
        String token = getAccessToken(authorizationHeader);

        // 가져온 토큰이 유효한지 확인하고, 유효한 때는 인증 정보 설정
        if(tokenProvider.validToken(token)) {
            Authentication authentication = tokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            // access 토큰이 만료되었을 경우, refresh 토큰을 사용하여 새로운 access 토큰 발급
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (currentAuth != null && currentAuth.isAuthenticated()) {
                Member member = (Member) currentAuth.getPrincipal();
                RefreshToken refreshToken = getRefreshTokenFromLoggedInMember();

                if (refreshToken != null && tokenProvider.validRefreshToken(refreshToken)) {
                    String newAccessToken = tokenProvider.generateToken(member, TokenProvider.ACCESS_TOKEN_DURATION);
                    Authentication newAuth = tokenProvider.getAuthentication(newAccessToken);
                    SecurityContextHolder.getContext().setAuthentication(newAuth);

                    response.setHeader(TokenProvider.HEADER_AUTHORIZATION, TokenProvider.TOKEN_PREFIX + " " + newAccessToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    private String getAccessToken(String authorizationHeader) {

        if(authorizationHeader != null && authorizationHeader.startsWith(TokenProvider.TOKEN_PREFIX)) {
            return authorizationHeader.substring(TokenProvider.TOKEN_PREFIX.length()).trim();
        }
        return null;
    }

    // 현재 로그인중인 회원의 refresh 토큰을 가져오는 메서드
    private RefreshToken getRefreshTokenFromLoggedInMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Member member = (Member) authentication.getPrincipal();
            return refreshTokenRepository.findByMemberNo(member.getMemberNo());
        }
        return null;
    }
}

