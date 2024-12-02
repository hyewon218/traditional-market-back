package com.market.global.visitor;

import com.market.global.security.CookieUtil;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
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

    private String createNewVisitorId(HttpServletResponse response) {
        String visitorId = UUID.randomUUID().toString();
        CookieUtil.addCookie(response, COOKIE_NAME, visitorId, (int) COOKIE_EXPIRATION);
        return visitorId;
    }

/*    // Redis 에 저장할 때 이미 유효 시간을 설정하므로 불필요함
    @Scheduled(cron = "0 0 0 * * *") // 매일 자정에 실행
    public void resetDailyVisitorCount() {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = "visitor:" + today;
        redisTemplate.delete(key);
    }*/

    ////////////////////// 쿠키 사용해 조회수 증가 시 필요///////////////////////////////
    public void markMarketAsViewed(String visitorId, Long marketNo) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = "marketViewed:" + visitorId + ":" + today;

        LocalDateTime midnight = LocalDateTime.now().plusDays(1).with(LocalTime.MIDNIGHT);
        long secondsUntilExpiration = Duration.between(LocalDateTime.now(), midnight).getSeconds();

        redisTemplate.opsForSet().add(key, marketNo.toString()); // 시장 번호를 집합에 추가
        redisTemplate.expire(key, secondsUntilExpiration, TimeUnit.SECONDS); // 만료 시간 설정
    }

    public boolean hasMarketBeenViewed(String visitorId, Long marketNo) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String key = "marketViewed:" + visitorId + ":" + today;
        return redisTemplate.opsForSet().isMember(key, marketNo.toString());
    }
    //////////////////////////////////////////////////////////////////////////////////
}
