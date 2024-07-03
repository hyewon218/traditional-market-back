package com.market.global.jwt.config;

import com.market.domain.member.entity.Member;
import com.market.domain.member.repository.MemberRepository;
import com.market.global.jwt.entity.RefreshToken;
import com.market.global.redis.RedisUtils;
import com.market.global.security.CookieUtil;
import com.market.global.security.UserDetailsServiceImpl;
import io.jsonwebtoken.*;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.time.Duration;
import java.util.Collections;
import java.util.Date;
import java.util.Set;

@Slf4j
@Service
@RequiredArgsConstructor
public class TokenProvider {

    private final UserDetailsServiceImpl userDetailsService;
    private final MemberRepository memberRepository;
    private final JwtProperties jwtProperties;
    private final RedisUtils redisUtils;
    public static final String HEADER_AUTHORIZATION = "Authorization";
    public static final String TOKEN_PREFIX = "Bearer ";
    public static final String REFRESH_TOKEN_COOKIE_NAME = "refresh_token";
    public static final Duration ACCESS_TOKEN_DURATION = Duration.ofMinutes(30);
    public static final Duration REFRESH_TOKEN_DURATION = Duration.ofHours(12); // 보통 14일, 수정하기

    public String generateToken(Member member, Duration expiredAt) {
        Date now = new Date();
        return makeToken(new Date(now.getTime() + expiredAt.toMillis()), member);
    }

    // access 토큰 생성 메서드
    private String makeToken(Date expiry, Member member) {
        Date now = new Date();

        return TOKEN_PREFIX + Jwts.builder()
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

    public boolean validToken(String accessToken) {

        if (accessToken == null || accessToken.isEmpty()) {
            log.error("토큰이 null이거나 빈 문자열입니다.");
            return false;
        }

        try {
            if (redisUtils.hasBlack(getMemberId(accessToken))) {
                log.info("memberId : " + getMemberId(accessToken)); // 지우기
                log.info("인증 요청하는 액세스토큰 : " + accessToken); // 지우기
                log.info("블랙리스트에 저장된 value : " + redisUtils.getBlackListValue(getMemberId(accessToken))); // 지우기
                log.info("다시 로그인 해주세요");
                return false;
            }
            Jwts.parser()
                    .setSigningKey(jwtProperties.getA_secret_key())
                    .build()
                    .parseClaimsJws(accessToken);

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

    // 생성된 access 토큰을 쿠키에 저장
    public void addTokenToCookie(HttpServletRequest request, HttpServletResponse response, String accessToken)
            throws UnsupportedEncodingException {
        int cookieMaxAge = (int) ACCESS_TOKEN_DURATION.toSeconds();
        String encodeCode = URLEncoder.encode(accessToken, "utf-8").replaceAll("\\+", "%20");
        CookieUtil.deleteCookie(request, response, HEADER_AUTHORIZATION);
        CookieUtil.addCookie(response, HEADER_AUTHORIZATION, encodeCode, cookieMaxAge);
    }


    // 생성된 refresh 토큰을 쿠키에 저장 // 관련해서 보안 설정 할거있는지 확인해야함
    public void addRefreshTokenToCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken)
            throws UnsupportedEncodingException {
        int cookieMaxAge = (int) REFRESH_TOKEN_DURATION.toSeconds();
        String encodeCode = URLEncoder.encode(refreshToken, "utf-8").replaceAll("\\+", "%20");
        CookieUtil.deleteCookie(request, response, REFRESH_TOKEN_COOKIE_NAME);
        CookieUtil.addCookie(response, REFRESH_TOKEN_COOKIE_NAME, encodeCode, cookieMaxAge);
    }

    // 쿠키에서 액세스토큰 가져오는 메서드
    public String getTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(HEADER_AUTHORIZATION)) {
                    try {
                        return URLDecoder.decode(cookie.getValue(), "UTF-8"); // Encode 되어 넘어간 Value 다시 Decode
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    // 쿠키에서 리프레시토큰 가져오는 메서드(만약 쿠키에 저장한다면)
    public String getRefreshTokenFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(REFRESH_TOKEN_COOKIE_NAME)) {
                    try {
                        return URLDecoder.decode(cookie.getValue(), "UTF-8"); // Encode 되어 넘어간 Value 다시 Decode
                    } catch (UnsupportedEncodingException e) {
                        return null;
                    }
                }
            }
        }
        return null;
    }

    // 액세스토큰, 리프레시토큰 재발급
    public String createNewAccessToken(String refreshToken, HttpServletRequest request, HttpServletResponse response)
            throws UnsupportedEncodingException {
        // 토큰 유효성 검사에 실패하면 예외 발생
        if(!validRefreshToken(refreshToken)) {
            throw new IllegalArgumentException("토큰이 유효하지않습니다.");
        }
        Long memberNo = getRefreshMemberNo(refreshToken);
        Member member = memberRepository.findById(memberNo)
                .orElseThrow(() -> new IllegalArgumentException("일치하는 회원이 없습니다"));

        // 액세스토큰 생성
        String newAccessToken = generateToken(member, ACCESS_TOKEN_DURATION);

        // 액세스토큰 쿠키에 설정
        addTokenToCookie(request, response, newAccessToken);

        // 리프레시토큰 재발급
        RefreshToken newRefreshToken = generateRefreshToken(member, REFRESH_TOKEN_DURATION);
        redisUtils.setValues(member.getMemberId(), newRefreshToken.getRefreshToken(), REFRESH_TOKEN_DURATION);

        // 리프레시토큰 쿠키에 설정 // 관련해서 보안 설정 할거있는지 확인해야함
        addRefreshTokenToCookie(request, response, newRefreshToken.getRefreshToken());

        return newAccessToken;
    }
}
