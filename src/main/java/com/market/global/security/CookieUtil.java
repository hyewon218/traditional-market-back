package com.market.global.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.SerializationUtils;

import java.util.Base64;
import java.util.Optional;

@Slf4j
public class CookieUtil {

    // 요청값(이름, 값, 만료기간)을 바탕으로 HTTP 응답에 쿠키 추가
    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        // HttpOnly 설정: 자바스크립트에서 쿠키를 접근하지 못하게 합니다.
//        cookie.setHttpOnly(true);

        // Secure 설정: HTTPS를 통해서만 쿠키가 전송되도록 합니다.
        // (개발 환경에서는 HTTPS가 아닐 수 있으므로, 상황에 맞게 설정)
//        cookie.setSecure(true);

        response.addCookie(cookie);
        if (cookie != null) {
            log.info("쿠키가 생성되었습니다 : " + cookie.getValue());
        } else {
            log.info("쿠키가 null입니다");
        }
    }

    public static void addCookieForRefreshToken(HttpServletResponse response, String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setPath("/");
        cookie.setMaxAge(maxAge);

        // HttpOnly 설정: 클라이언트측 자바스크립트에서 쿠키를 접근하지 못하게 합니다.
        cookie.setHttpOnly(true);

        // Secure 설정: HTTPS를 통해서만 쿠키가 전송되도록 합니다.
        // (개발 환경에서는 HTTPS가 아닐 수 있으므로, 상황에 맞게 설정)
//        cookie.setSecure(true);

        response.addCookie(cookie);
        if (cookie != null) {
            log.info("쿠키가 생성되었습니다 : " + cookie.getValue());
        } else {
            log.info("쿠키가 null입니다");
        }
    }

//    public static void addCookie(HttpServletResponse response, String name, String value, int maxAge,
//                                 boolean httpOnly, boolean secure) {
//        Cookie cookie = new Cookie(name, value);
//        cookie.setPath("/");
//        cookie.setMaxAge(maxAge);
//        cookie.setHttpOnly(httpOnly); // JavaScript에서 쿠키를 사용하여 세션 관리를 해야 할 경우에는 HTTPOnly를 사용할 수 없음
//        cookie.setSecure(secure); // https에서만 동작
//        response.addCookie(cookie);
//    }

    // 쿠키의 이름을 입력받아 쿠키 삭제(실제로는 삭제할 수 없어서 빈값으로 바꾸고 만료시간 0으로 설정해 재생성 되자마자 만료 처리)
    public static void deleteCookie(HttpServletRequest request, HttpServletResponse response, String name) {

        Cookie[] cookies = request.getCookies();
        if (cookies == null) {
            return;
        }

        for(Cookie cookie : cookies) {
            if (name.equals(cookie.getName())) {
                cookie.setValue("");
                cookie.setPath("/");
                cookie.setMaxAge(0);
                response.addCookie(cookie);
            }
        }
    }

    // 객체를 직렬화해 쿠키의 값으로 반환
    public static String serialize(Object obj) {
        return Base64.getUrlEncoder()
                .encodeToString(SerializationUtils.serialize(obj));
    }

    // 쿠키를 역직렬화해 객체로 변환
    public static <T> T deserialize(Cookie cookie, Class<T> cls) {
        return cls.cast(
                SerializationUtils.deserialize(
                        Base64.getUrlDecoder().decode(cookie.getValue())
                )
        );
    }

    public static Optional<Cookie> getCookie(HttpServletRequest request, String name) {
        Cookie[] cookies = request.getCookies();

        if (cookies != null && cookies.length > 0) {
            for (Cookie cookie : cookies) {
                if (cookie.getName().equals(name)) {
                    return Optional.of(cookie);
                }
            }
        }
        return Optional.empty();
    }
}
