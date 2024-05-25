package com.market.global.jwt.config;

import com.market.domain.member.entity.Member;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.jwt.repository.RefreshTokenRepository;
import com.market.global.redis.RedisUtils;
import com.market.global.security.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final UserDetailsServiceImpl userDetailsService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtProperties jwtProperties;
    private final RedisUtils redisUtils;
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer";
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(15);
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofHours(12); // 보통 14일, 수정하기

    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), member);
    }

    // access 토큰 생성 메서드
    private String makeToken(Date expiry, Member member) {
        Date now = new Date();

        return Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .setSubject(member.getMemberId())
                .claim("no", member.getMemberNo())
                .claim("role", "ROLE_" + member.getRole().name())
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getA_secret_key())
                .compact();
    }

    // Refresh 토큰 생성 메서드
    public RefreshToken generateRefreshToken(Member member, Duration expiredAt) {
        Date now = new Date();

        String refreshToken = Jwts.builder()
                .setHeaderParam(Header.TYPE, Header.JWT_TYPE)
                .setIssuer(jwtProperties.getIssuer())
                .setIssuedAt(now)
                .setExpiration(new Date(now.getTime() + expiredAt.toMillis()))
                .claim("no", member.getMemberNo())
                .signWith(SignatureAlgorithm.HS256, jwtProperties.getR_secret_key())
                .compact();

        return new RefreshToken(member.getMemberNo(), refreshToken);
    }

    public boolean validToken(String token) {

        if (token == null || token.isEmpty()) {
            log.error("토큰이 null이거나 빈 문자열입니다.");
            return false;
        }

        try {

            if (redisUtils.hasBlack(token)) {
                log.info("다시 로그인 해주세요");
                return false;
            }

            Jwts.parser()
                    .setSigningKey(jwtProperties.getA_secret_key())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.error("잘못된 JWT 서명입니다.", e);
        } catch (ExpiredJwtException e) {
            log.error("만료된 JWT 토큰입니다.", e);
        } catch (UnsupportedJwtException e) {
            log.error("지원되지 않는 JWT 토큰입니다.", e);
        } catch (IllegalArgumentException e) {
            log.error("JWT 토큰이 잘못되었습니다.", e);
        }
        return false;
    }

    public boolean validRefreshToken(String refreshToken) {

        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getR_secret_key())
                    .build()
                    .parseClaimsJws(refreshToken);

            return true;

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 refresh 토큰 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 refresh 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 refresh 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("refresh 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public String getAccessToken(String authorizationHeader) {

        if (authorizationHeader != null && authorizationHeader.startsWith(TokenProvider.TOKEN_PREFIX)) {
            return authorizationHeader.substring(TokenProvider.TOKEN_PREFIX.length()).trim();
        }
        return null;
    }

    // 토큰 기반으로 인증된 유저 정보 가져오는 메서드
    public Authentication getAuthentication(String token) {

        Claims claims = getClaims(token);
        String role = claims.get("role", String.class); // 토큰에서 role 추출
        Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(role));

        UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

        return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
    }

    // 토큰으로 유저 고유번호를 가져오는 메서드
    public Long getMemberNo(String token) {
        Claims claims = getClaims(token);
        return claims.get("no", Long.class);
    }

    // 테스트용, 지우기
    public Long getRefreshMemberNo(String token) {
        Claims claims = getRefreshClaims(token);
        return claims.get("no", Long.class);
    }

    // 토큰으로 사용자 아이디 가져오는 메서드
    public String getMemberId(String token) {
        Claims claims = getClaims(token);
        return claims.getSubject();
    }

    private Claims getClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getA_secret_key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // 테스트용, 지우기
    private Claims getRefreshClaims(String token) {
        return Jwts.parser()
                .setSigningKey(jwtProperties.getR_secret_key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // access 토큰 남은 유효시간
    public long getExpiration(String token) {
        // 만료시간
        Date expiration = Jwts.parser()
                .setSigningKey(jwtProperties.getA_secret_key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        // 현재시간
        Date now = new Date();
        return (expiration.getTime() - now.getTime());
    }

}
