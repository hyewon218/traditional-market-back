package com.market.global.jwt.config;

import com.market.domain.member.entity.Member;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.jwt.repository.RefreshTokenRepository;
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
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer";
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofHours(2); // 보통 30분, 1시간으로 수정하기
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofHours(12); // 보통 14일, 7일로 수정하기

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

        return refreshTokenRepository.save(new RefreshToken(member.getMemberNo(), refreshToken));
    }

    // access 토큰 유효성 검증 메서드
    public boolean validToken(String token) {

        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getA_secret_key())
                    .build()
                    .parseClaimsJws(token);

            return true;

        } catch (io.jsonwebtoken.security.SecurityException | MalformedJwtException e) {
            log.info("잘못된 JWT 서명입니다.");
        } catch (ExpiredJwtException e) {
            log.info("만료된 JWT 토큰입니다.");
        } catch (UnsupportedJwtException e) {
            log.info("지원되지 않는 JWT 토큰입니다.");
        } catch (IllegalArgumentException e) {
            log.info("JWT 토큰이 잘못되었습니다.");
        }
        return false;
    }

    public boolean validRefreshToken(RefreshToken refreshToken) {

        try {
            Jwts.parser()
                    .setSigningKey(jwtProperties.getR_secret_key())
                    .build()
                    .parseClaimsJws(refreshToken.getRefreshToken());

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
    
    // 토큰 기반으로 인증된 유저 정보 가져오는 메서드
    public Authentication getAuthentication(String token) {

    Claims claims = getClaims(token);
    String role = claims.get("role", String.class); // 토큰에서 role 추출
    Set<SimpleGrantedAuthority> authorities = Collections.singleton(new SimpleGrantedAuthority(role));

    UserDetails userDetails = userDetailsService.loadUserByUsername(claims.getSubject());

    return new UsernamePasswordAuthenticationToken(userDetails, token, authorities);
}

    // 토큰 기반으로 유저 고유번호를 가져오는 메서드
    public Long getMemberNo(String token) {
        Claims claims = getClaims(token);
        return claims.get("no", Long.class);
    }

    private Claims getClaims(String token) {
        return  Jwts.parser()
                .setSigningKey(jwtProperties.getA_secret_key())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    // access 토큰 만료시간
    public Duration getExpiration(String token) {
        Date expiration = Jwts.parser()
                .setSigningKey(jwtProperties.getA_secret_key())
                .build()
                .parseClaimsJws(token)
                .getBody()
                .getExpiration();

        // 현재시간
        Date now = new Date();
        return Duration.ofHours((expiration.getTime() - now.getTime()));
    }



}
