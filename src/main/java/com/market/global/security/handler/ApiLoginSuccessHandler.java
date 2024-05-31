package com.market.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.member.dto.MemberResponseDto;
import com.market.domain.member.entity.Member;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.jwt.repository.RefreshTokenRepository;
import com.market.global.redis.RedisUtils;
import com.market.global.security.UserDetailsImpl;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

import java.io.IOException;


@Log4j2 // Sl4j와 차이점?
@RequiredArgsConstructor
public class ApiLoginSuccessHandler implements AuthenticationSuccessHandler {

    private final TokenProvider tokenProvider;
    private final RefreshTokenRepository refreshTokenRepository;
    private final ObjectMapper objectMapper;
    private final RedisUtils redisUtils;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("=========================================================");
        log.info(authentication);
        log.info("=========================================================");

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Member member = userDetails.getMember();

        // access 토큰 생성
        String accessToken = tokenProvider.generateToken(member, TokenProvider.ACCESS_TOKEN_DURATION);
        tokenProvider.addTokenToCookie(request, response, accessToken);
        log.info("access 토큰이 생성되었습니다 : " + accessToken);
        String cookie = tokenProvider.getTokenFromCookie(request);
        if (cookie == null) {
            log.info("쿠키가 null입니다");
        }
        log.info("cookie : " + cookie);


        // refresh 토큰 생성(refresh 토큰 없거나 유효하지 않을 경우)
        String findRefreshToken = redisUtils.getValues(member.getMemberId());

        // refresh 토큰 없는 경우
        if (findRefreshToken == null) {
            RefreshToken refreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
            redisUtils.setValues(member.getMemberId(), refreshToken.getRefreshToken(), TokenProvider.REFRESH_TOKEN_DURATION);
            log.info("refresh 토큰이 생성되었습니다(생성된 토큰) : " + refreshToken.getRefreshToken());
            log.info("refresh 토큰이 생성되었습니다(redis에서 가져온 토큰) : " + redisUtils.getValues(member.getMemberId()));
            
        // refresh 토큰이 유효하지않은 경우    
        } else if (!tokenProvider.validRefreshToken(findRefreshToken)) {
            redisUtils.deleteValues(member.getMemberId());
            RefreshToken newRefreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
            redisUtils.setValues(member.getMemberId(), newRefreshToken.getRefreshToken(), TokenProvider.REFRESH_TOKEN_DURATION);
            log.info("refresh 토큰이 생성되었습니다(생성된 토큰) : " + newRefreshToken.getRefreshToken());
            log.info("refresh 토큰이 생성되었습니다(redis에서 가져온 토큰) : " + redisUtils.getValues(member.getMemberId()));
        }

        MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                    .memberNo(member.getMemberNo())
                    .memberId(member.getMemberId())
                    .memberEmail(member.getMemberEmail())
                    .memberNickname(member.getMemberNickname())
                    .memberPw(member.getMemberPw())
                    .providerType(member.getProviderType())
                    .role(member.getRole())
                    .createTime(member.getCreateTime())
                    .updateTime(member.getUpdateTime())
                    .accessToken(accessToken)
                    .refreshToken(redisUtils.getValues(member.getMemberId())) // 삭제할 것
                    .build();

        // memberResponseDto 객체를 JSON 문자열로 직렬화
        String jsonResponse = objectMapper.writeValueAsString(memberResponseDto);
        // HTTP 응답 헤더 설정, 클라이언트에게 반환되는 데이터 json 타입
        response.setContentType("application/json");
        // HTTP 응답 본문에 json 문자열 작성, json 데이터를 클라이언트에게 전송
        response.getWriter().write(jsonResponse);
    }
}
