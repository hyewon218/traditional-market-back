package com.market.global.visitor;

import com.market.global.redis.RedisUtils;
import com.market.global.security.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class VisitorService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");
    private static final String COOKIE_NAME = "visitor_id";
    private static final long COOKIE_EXPIRATION = 60 * 60 * 24; // 1일
    private static final String TOTAL_VISITOR_KEY = "total_visitors_hll";

    public void trackVisitor(HttpServletRequest request, HttpServletResponse response) {
        String visitorId = getVisitorIdFromCookie(request);
        if(visitorId == null) {
            visitorId = createNewVisitorId(response);
        }

        // 현재 시간에서 자정까지의 시간 간격을 계산하여 만료 시간을 설정
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).with(LocalTime.MIDNIGHT);
        long secondsUntilExpiration = Duration.between(LocalDateTime.now(), midnight).getSeconds();

        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = "visitor:" + today;

        redisTemplate.opsForSet().add(key, visitorId);
        redisTemplate.expire(key, secondsUntilExpiration, TimeUnit.SECONDS);
        redisTemplate.opsForHyperLogLog().add(TOTAL_VISITOR_KEY, visitorId);
    }

    public long getTodayVisitorCount() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = "visitor:" + today;
        Long count = redisTemplate.opsForSet().size(key);
        return count != null ? count : 0;
    }

    public long getTotalVisitorCount() {
        return redisTemplate.opsForHyperLogLog().size(TOTAL_VISITOR_KEY);
    }

    private String getVisitorIdFromCookie(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if(cookies != null) {
            for(Cookie cookie : cookies) {
                if (COOKIE_NAME.equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }

//    private String createNewVisitorId(HttpServletResponse response) {
//        String visitorId = UUID.randomUUID().toString();
//        Cookie cookie = new Cookie(COOKIE_NAME, visitorId);
//        cookie.setPath("/");
//        cookie.setMaxAge((int) COOKIE_EXPIRATION);
//        response.addCookie(cookie);
//        return visitorId;
//    }

    private String createNewVisitorId(HttpServletResponse response) {
        String visitorId = UUID.randomUUID().toString();
        CookieUtil.addCookie(response, COOKIE_NAME, visitorId, (int) COOKIE_EXPIRATION);
        return visitorId;
    }

    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void resetDailyVisitorCount() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = "visitor:" + today;
        redisTemplate.delete(key);
    }
}