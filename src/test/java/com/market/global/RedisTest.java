package com.market.global;

import com.market.domain.member.constant.Role;
import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.jwt.config.JwtProperties;
import com.market.global.jwt.config.TokenProvider;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.redis.RedisUtils;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;

@Slf4j
@SpringBootTest
class RedisTest {

    private Member member;
    private String accessToken;
    private String refreshToken;
    private RefreshToken refreshTokenObject;

    @Autowired
    private RedisUtils redisUtils;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private TokenProvider tokenProvider;

    @Autowired
    private JwtProperties jwtProperties;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setData() {
        memberRepository.deleteAll();
        member = Member.builder()
                .memberId("song1")
                .memberPw(passwordEncoder.encode("1234"))
                .memberEmail("song1@email.com")
                .role(Role.MEMBER)
                .build();
        memberRepository.save(member);

        accessToken = tokenProvider.generateToken(member, TokenProvider.ACCESS_TOKEN_DURATION);
        refreshTokenObject = tokenProvider.generateRefreshToken(member, TokenProvider.REFRESH_TOKEN_DURATION);
        redisUtils.setValues(member.getMemberId(), refreshTokenObject.getRefreshToken());
        refreshToken = redisUtils.getValues(member.getMemberId());
    }

    @Test
    @DisplayName("Redis에 데이터를 저장하면 정상적으로 조회된다")
    void saveAndFindTest() throws Exception {

        Authentication authentication = tokenProvider.getAuthentication(accessToken);
        Long findMemberNo = tokenProvider.getMemberNo(accessToken);
        Long findRefreshMemberNo = tokenProvider.getRefreshMemberNo(refreshToken);

        assertThat(refreshTokenObject.getRefreshToken()).isEqualTo(refreshToken);
        log.info("액세스토큰 : " + accessToken);
        log.info("리프레시토큰 : " + refreshTokenObject.getRefreshToken());
        log.info("리프레시토큰 entity 내 회원번호 : " + refreshTokenObject.getMemberNo());
        log.info("redis에 저장된 리프레시토큰 : " + refreshToken);
        log.info("액세스토큰에서 찾은 회원 번호 : " + findMemberNo);
        log.info("액세스토큰에서 찾은 회원 아이디: " + (((UserDetails)authentication.getPrincipal()).getUsername()));
        log.info("권한 : " + authentication.getAuthorities());
        log.info("리프레시토큰에서 찾은 회원 번호 : " + findRefreshMemberNo);
    }

    @Test
    @DisplayName("Redis에 저장된 데이터를 수정할 수 있다")
    void updateTest() throws Exception {
        // given
        String updateValue = "updateValue";
        redisUtils.setValues(member.getMemberId(), updateValue);

        // when
        String findValue = redisUtils.getValues(member.getMemberId());

        // then
        assertThat(updateValue).isEqualTo(findValue);
    }

    @Test
    @DisplayName("Redis의 blacklist에 access 토큰을 등록해 무효화할 수 있다")
    void setBlackListTest() {

        // 블랙리스트에 access 토큰 저장
        redisUtils.setBlackList(member.getMemberId(), accessToken, tokenProvider.getExpiration(accessToken));

        log.info("일반에 저장된 엑세스토큰 : " + refreshToken);

        // 블랙리스트에 저장된 access 토큰 확인
        String blackAccessToken = redisUtils.getBlackList(member.getMemberId());
        log.info("블랙리스트에 저장된 엑세스토큰 : " + blackAccessToken);

        // 블랙리스트에 "black" 있는지 확인
        if (blackAccessToken != null) {
            boolean existsBlackKey = redisUtils.hasBlack(blackAccessToken);
            log.info("블랙리스트에 \"black\" 있는지 확인(있으면 true) : " + existsBlackKey);
        }

        // 블랙리스트에 저장된 access 토큰으로 유효성 검증
        boolean checkValid = tokenProvider.validToken(blackAccessToken);
        log.info("유효성 검증 결과(블랙리스트에 등록돼있으면 false) : " + checkValid);

//        assertThat(existsBlackKey).isTrue();
        assertThat(checkValid).isFalse();
    }

    @Test
    @DisplayName("Redis에 저장된 데이터를 삭제할 수 있다")
    void deleteTest() throws Exception {
        // refresh 토큰은 redis에서 삭제하고 hasKey로 확인하기
        redisUtils.deleteValues(member.getMemberId());

        boolean existsRefresh = redisUtils.hasKey(member.getMemberId());
        log.info("리프레시토큰 삭제 후 있는지 확인(없으면 false) : " + existsRefresh);

        String deletedRefresh = redisUtils.getValues(member.getMemberId());
        log.info("리프레시토큰 : " + deletedRefresh);

        assertThat(existsRefresh).isFalse();
        assertThat(deletedRefresh).isEqualTo(null);
    }


}
