package com.market.global.ip;

import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class IpService { // 공지사항, 시장, 상점 조회 시 클라이언트 IP 주소 이용한 조회수 관련 클래스

    private final RedisTemplate<String, String> redisTemplate;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyyMMdd");

    // 아래 2개 조회수 증가 메서드
    // ipAddress를 key로 하는 집합에 typeNo 추가(아래 hasMarketBeenViewed 메서드 결과 false라면 실행)
    public void markTypeAsViewed(String ipAddress, String type, Long typeNo) {
        String key = generateKey(ipAddress, type);

        // 현재 시간에서 자정까지의 시간 간격을 계산하여 만료 시간을 설정
        LocalDateTime midnight = LocalDateTime.now().plusDays(1).with(LocalTime.MIDNIGHT);
        long secondsUntilExpiration = Duration.between(LocalDateTime.now(), midnight).getSeconds();

        redisTemplate.opsForSet().add(key, typeNo.toString()); // 공지사항 번호를 집합에 추가
        redisTemplate.expire(key, secondsUntilExpiration, TimeUnit.SECONDS); // 만료 시간 설정
    }

    // ipAddress를 key로 하는 집합에 typeNo이 있는지 확인
    public boolean hasTypeBeenViewed(String ipAddress, String type, Long typeNo) {
        String key = generateKey(ipAddress, type);
        return redisTemplate.opsForSet().isMember(key, typeNo.toString());
    }

    // switch 문을 사용하여 키 생성
    private String generateKey(String ipAddress, String type) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        return switch (type) {
            case "notice" -> "noticeViewed:" + ipAddress + "_" + today;
            case "market" -> "marketViewed:" + ipAddress + "_" + today;
            case "shop" -> "shopViewed:" + ipAddress + "_" + today;
            case "item" -> "itemViewed:" + ipAddress + "_" + today;
            default -> throw new IllegalArgumentException("Unsupported type: " + type);
        };
    }

    // HttpServletRequest에서 IP 주소를 추출하는 메서드
    // localhost로 접속 시 ip를 가져오면 0:0:0:0:0:0:0:1로 출력됨
    // ip로 접속하면 정상적인 ip로 얻어옴
    public String getIpAddress(HttpServletRequest request) {
        String ipAddress = request.getHeader("X-Forwarded-For");
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ipAddress == null || ipAddress.isEmpty() || "unknown".equalsIgnoreCase(ipAddress)) {
            ipAddress = request.getRemoteAddr();
        }
        return ipAddress;
    }

}
