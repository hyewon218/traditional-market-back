package com.market.global.config.jwt;

import com.market.global.jwt.config.JwtProperties;
import com.market.global.jwt.config.TokenProvider;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import io.jsonwebtoken.Jwts;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;

import java.time.Duration;
import java.util.Date;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class TokenProviderTest {

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private JwtProperties jwtProperties;

    // generateToken() 검증 테스트
    @DisplayName("generateToken(): 회원 정보와 만료 기간을 전달해 토큰을 만들 수 있다")
    @Test
    void generateToken() {
        // given
        Member testMember = memberRepository.save(Member.builder()
                .memberId("song7")
                .memberEmail("song7@email.com")
                .memberPw("1234")
                .build());
        
        // when
        String token = tokenProvider.generateToken(testMember, Duration.ofDays(14));

        // then
        Long memberNo = Jwts.parser()
                .setSigningKey(jwtProperties.getA_secret_key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .get("no", Long.class);

        assertThat(memberNo).isEqualTo(testMember.getMemberNo());
    }

    // validToken() 검증 테스트
    @DisplayName("validToken(): 만료된 토큰인 때에 유효성 검증에 실패한다")
    @Test
    void validToken_invalidToken() {
        // given
        String token = JwtFactory.builder()
                .expiration(new Date(new Date().getTime() - Duration.ofDays(7).toMillis()))
                .build()
                .createToken(jwtProperties);

        // when
        boolean result = tokenProvider.validToken(token);

        // then
        assertThat(result).isFalse();
    }

    @DisplayName("validToken(): 유효한 토큰인 때에 유효성 검증에 성공한다")
    @Test
    void validToken_validation() {
        // given
        String token = JwtFactory.withDefaultValues()
                .createToken(jwtProperties);

        // when
        boolean result = tokenProvider.validToken(token);

        // then
        assertThat(result).isTrue();
    }

    // getAuthentication() 검증 테스트
    @DisplayName("getAuthentication(): 토큰 기반으로 인증 정보를 가져올 수 있다")
    @Test
    void getAuthentication() {
        // given
        String memberId = "song101";
        String token = JwtFactory.builder()
                .subject(memberId)
                .build()
                .createToken(jwtProperties);

        // when
        Authentication authentication = tokenProvider.getAuthentication(token);

        // then
        assertThat(((UserDetails) authentication.getPrincipal()).getUsername()).isEqualTo(memberId);
    }

    // getMemberNo() 검증 테스트
    @DisplayName("getMemberNo: 토큰으로 회원 No를 가져올 수 있다")
    @Test
    void getMemberNo() {
        // given
        Long memberNo = 100L;
        String token = JwtFactory.builder()
                .claims(Map.of("no", memberNo))
                .build()
                .createToken(jwtProperties);

        // when
        Long memberNoByToken = tokenProvider.getMemberNo(token);

        // then
        assertThat(memberNoByToken).isEqualTo(memberNo);
    }
}
