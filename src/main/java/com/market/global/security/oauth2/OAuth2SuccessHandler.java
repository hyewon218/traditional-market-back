package com.market.global.security.oauth2;

import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.redis.RedisUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.client.authentication.OAuth2AuthenticationToken;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.util.UriComponentsBuilder;

import java.io.IOException;
import java.util.Optional;

@RequiredArgsConstructor
@Component
@Slf4j
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    public static final String FIRST_REDIRECT_PATH = "/auth/success";
    public static final String REDIRECT_PATH = "/";

    private final TokenProvider tokenProvider;
    private final OAuth2AuthorizationRequestBasedOnCookieRepository authorizationRequestRepository;
    private final MemberRepository memberRepository;
    private final RedisUtils redisUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2AuthenticationToken authToken = (OAuth2AuthenticationToken) authentication;
        ProviderType providerType = ProviderType.valueOf(
                authToken.getAuthorizedClientRegistrationId().toUpperCase());

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        OAuth2UserInfo userInfo = OAuth2UserInfo.of(providerType, oAuth2User.getAttributes());
//        Member member = memberRepository.findByMemberEmail((String) userInfo.getAttributes().get("email"));
        Optional<Member> optionalMember = memberRepository.findByMemberEmail(userInfo.memberEmail());
        if (optionalMember.isEmpty()) {
            throw new IllegalArgumentException("해당 이메일을 가진 회원을 찾을 수 없습니다");
        }
        Member member = optionalMember.get();

        // 닉네임 있는지 없는지 확인 후 Redirect 경로 설정
        String redirectPath = (member.getMemberNickname() == null) ? FIRST_REDIRECT_PATH : REDIRECT_PATH;

        // 액세스토큰 생성
        String accessToken = tokenProvider.generateToken(member, TokenProvider.ACCESS_TOKEN_DURATION);
        log.info("access 토큰이 생성되었습니다 : " + accessToken);
        
        // 쿠키 생성
        tokenProvider.addTokenToCookie(request, response, accessToken);

        // redirect url 설정
        String targetUrl = getTargetUrl(redirectPath, accessToken);

        // refresh 토큰 생성(refresh 토큰 없거나 유효하지 않을 경우)
        String findRefreshToken = redisUtils.getValues(member.getMemberId());

        // refresh 토큰 없는 경우
        if (findRefreshToken == null) {
            RefreshToken refreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
            redisUtils.setValues(member.getMemberId(), refreshToken.getRefreshToken(), TokenProvider.REFRESH_TOKEN_DURATION);
            tokenProvider.addRefreshTokenToCookie(request, response, refreshToken.getRefreshToken());
            log.info("refresh 토큰이 생성되었습니다(생성된 토큰) : " + refreshToken.getRefreshToken());
            log.info("refresh 토큰이 생성되었습니다(redis에서 가져온 토큰) : " + redisUtils.getValues(member.getMemberId()));

            // refresh 토큰이 유효하지않은 경우
        } else if (!tokenProvider.validRefreshToken(findRefreshToken)) {
            redisUtils.deleteValues(member.getMemberId());
            RefreshToken newRefreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
            redisUtils.setValues(member.getMemberId(), newRefreshToken.getRefreshToken(), TokenProvider.REFRESH_TOKEN_DURATION);
            tokenProvider.addRefreshTokenToCookie(request, response, newRefreshToken.getRefreshToken());
            log.info("refresh 토큰이 생성되었습니다(생성된 토큰) : " + newRefreshToken.getRefreshToken());
            log.info("refresh 토큰이 생성되었습니다(redis에서 가져온 토큰) : " + redisUtils.getValues(member.getMemberId()));
        }
        clearAuthenticationAttributes(request, response);
        getRedirectStrategy().sendRedirect(request, response, targetUrl);
    }

    // 인증 관련 설정값, 쿠키 제거
    private void clearAuthenticationAttributes(HttpServletRequest request,
                                               HttpServletResponse response) {
        super.clearAuthenticationAttributes(request);
        authorizationRequestRepository.removeAuthorizationRequestCookies(request, response);
    }

    // 토큰 기반으로 리디렉션할 URL 생성하는 메서드
    private String getTargetUrl(String redirectPath, String accessToken) {
        return UriComponentsBuilder.fromUriString(redirectPath)
//                .queryParam("accessToken", accessToken) // JWT 액세스토큰값을 url에 추가, 주석처리 해야함, 테스트로 켜놓음
                .build()
                .toUriString();
    }
}