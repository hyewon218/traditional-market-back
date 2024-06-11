package com.market.global.jwt.config;

import com.market.domain.member.entity.Member;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.redis.RedisUtils;
import com.market.global.visitor.VisitorService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Slf4j
@RequiredArgsConstructor
public class TokenAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final RedisUtils redisUtils;
    private final VisitorService visitorService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        String uri = request.getRequestURI();
        if (uri.equals("/api/members/signup") || uri.equals("/auth/success") ||
                uri.equals("/login/oauth2/code/*") || uri.equals("/members/login") || uri.equals("/api/send-mail/email")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 로그인, 비로그인 모두 방문자 쿠키 생성
        visitorService.trackVisitor(request, response);

        // 요청 헤더와 Authorization 키의 값
        String authorizationHeader = request.getHeader(TokenProvider.HEADER_AUTHORIZATION);

        // 가져온 값에서 접두사 제거
        String accessToken = tokenProvider.getAccessToken(authorizationHeader);

        // 가져온 토큰이 유효한지 확인하고, 유효한 때는 인증 정보 설정
        if (tokenProvider.validToken(accessToken)) {
            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

        } else {
            // access 토큰이 만료되었을 경우, refresh 토큰을 사용하여 새로운 access 토큰 발급
            // 이후 기존 refresh 토큰 삭제 후 재발급
            Authentication currentAuth = SecurityContextHolder.getContext().getAuthentication();

            if (currentAuth != null && currentAuth.isAuthenticated()) {
                Member member = (Member) currentAuth.getPrincipal();
                String refreshToken = getRefreshTokenFromLoggedInMember();

                if (refreshToken != null && tokenProvider.validRefreshToken(refreshToken)) {
                    String newAccessToken = tokenProvider.generateToken(member, TokenProvider.ACCESS_TOKEN_DURATION);
                    tokenProvider.addTokenToCookie(request, response, newAccessToken);
                    Authentication newAuth = tokenProvider.getAuthentication(newAccessToken);
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                    // 기존 refresh 토큰 삭제 후 다시 발급, 저장
                    redisUtils.deleteValues(member.getMemberId());
                    RefreshToken newRefreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
                    redisUtils.setValues(member.getMemberId(), newRefreshToken.getRefreshToken());
//                    response.setHeader(TokenProvider.HEADER_AUTHORIZATION, TokenProvider.TOKEN_PREFIX + " " + newAccessToken);
                    response.setHeader(TokenProvider.HEADER_AUTHORIZATION, newAccessToken);
                }
            }
        }
        filterChain.doFilter(request, response);
    }

    // 현재 로그인중인 회원의 refresh 토큰을 가져오는 메서드
    private String getRefreshTokenFromLoggedInMember() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication != null && authentication.isAuthenticated()) {
            Member member = (Member) authentication.getPrincipal();
            return redisUtils.getValues(member.getMemberId());
        }
        return null;
    }
}

