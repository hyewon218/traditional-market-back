package com.market.global.jwt.config;

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
    private final VisitorService visitorService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
        FilterChain filterChain)
        throws ServletException, IOException {

        String uri = request.getRequestURI();
        String method = request.getMethod();
        if (uri.equals("/api/members/signup") || uri.equals("/members/signup") ||
            uri.equals("/auth/success") || uri.equals("/api/members/checkid") ||
            uri.equals("/login/oauth2/code/*") || uri.equals("/members/login") ||
            uri.equals("/api/send-mail/email") || uri.equals("/api/members/login") ||
            uri.equals("/api/members/checkemail") || uri.equals("/api/members/verifycode") ||
            uri.equals("/api/markets") || uri.equals("/api/notices") ||
            uri.equals("/api/notices/search") ||
            (uri.startsWith("/api/shops") && method.equalsIgnoreCase("get")) ||
            (uri.startsWith("/api/") && uri.endsWith("/shops")
                && method.equalsIgnoreCase("get")) ||
            (uri.startsWith("/api/") && uri.endsWith("/items")
                && method.equalsIgnoreCase("get")) ||
            (uri.startsWith("/api/") && uri.endsWith("/comments")
                && method.equalsIgnoreCase("get")) ||
            (uri.startsWith("/api/") && uri.endsWith("/likes-count")) ||
            (uri.startsWith("/api/") && uri.endsWith("/category")) ||
            (uri.startsWith("/api/") && uri.endsWith("/category-by-shop")) ||
            (uri.startsWith("/api/") && uri.endsWith("/rank"))) {
            filterChain.doFilter(request, response);
            return;
        }

        // 로그인, 비로그인 모두 방문자 쿠키 생성
        visitorService.trackVisitor(request, response);

        final String authorizationHeader;
        // 요청 헤더와 Authorization 키의 값
        if (uri.equals("/api/notifications/subscribe")) {
            authorizationHeader = request.getHeader(TokenProvider.HEADER_AUTHORIZATION);
        } else {
            authorizationHeader = tokenProvider.getTokenFromCookie(request);
        }

        // 가져온 값에서 접두사 제거
        String accessToken = tokenProvider.getAccessToken(authorizationHeader);

        // 가져온 토큰이 유효한지 확인하고, 유효한 때는 인증 정보 설정
        if (tokenProvider.validToken(accessToken)) {
            Authentication authentication = tokenProvider.getAuthentication(accessToken);
            SecurityContextHolder.getContext().setAuthentication(authentication);

            // 액세스토큰 만료 시 리프레시토큰 이용해 새로운 액세스토큰 발급
        } else {
            String refreshToken = tokenProvider.getRefreshTokenFromCookie(request);

            if (refreshToken != null && tokenProvider.validRefreshToken(refreshToken)) {
                // 새로운 액세스토큰 발급과 동시에 리프레시토큰도 재발급
                String newAccessToken = tokenProvider.createNewAccessToken(refreshToken, request,
                    response);
                String realNewAccessToken = tokenProvider.getAccessToken(
                    newAccessToken); // Bearer 제거

                // 새로운 인증정보 저장
                Authentication newAuth = tokenProvider.getAuthentication(realNewAccessToken);
                SecurityContextHolder.getContext().setAuthentication(newAuth);

            } else if (refreshToken == null || tokenProvider.validRefreshToken(refreshToken)) {
                log.info("리프레시토큰이 null이거나 유효하지않습니다. 다시 로그인 해주세요");
            }
        }
        filterChain.doFilter(request, response);
    }
}


