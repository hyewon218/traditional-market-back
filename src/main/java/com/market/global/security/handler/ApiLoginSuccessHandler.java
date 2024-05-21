package com.market.global.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.member.dto.MemberResponseDto;
import com.market.domain.member.entity.Member;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.jwt.repository.RefreshTokenRepository;
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

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        log.info("=========================================================");
        log.info(authentication);
        log.info("=========================================================");

        UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();
        Member member = userDetails.getMember();

        // access 토큰 생성
        String accessToken = tokenProvider.generateToken(member, TokenProvider.ACCESS_TOKEN_DURATION);
        log.info("access 토큰이 생성되었습니다 : " + accessToken);

        // refresh 토큰 생성(refresh 토큰 없거나 유효하지 않을 경우)
        RefreshToken findRefreshToken = refreshTokenRepository.findByMemberNo(member.getMemberNo());
        if (findRefreshToken == null) {
            RefreshToken refreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
            log.info("refresh 토큰이 생성되었습니다 : " + refreshToken.getRefreshToken());

        } else if (!tokenProvider.validRefreshToken(findRefreshToken)) {
            refreshTokenRepository.delete(findRefreshToken);
            RefreshToken newRefreshToken = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
            log.info("refresh 토큰이 생성되었습니다 : " + newRefreshToken.getRefreshToken());
        }

        MemberResponseDto memberResponseDto = MemberResponseDto.builder()
                    .memberNo(member.getMemberNo())
                    .memberId(member.getMemberId())
                    .memberPw(member.getMemberPw())
                    .memberEmail(member.getMemberEmail())
                    .role(member.getRole())
                    .createTime(member.getCreateTime())
                    .updateTime(member.getUpdateTime())
                    .token(accessToken)
                    .build();

        String jsonResponse = objectMapper.writeValueAsString(memberResponseDto);
        response.setContentType("application/json");
        response.getWriter().write(jsonResponse);
    }
}
