package com.market.global.visitor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Slf4j
class VisitorControllerTest {

    @Autowired
    private VisitorService visitorService;

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @BeforeEach
    void setUp() {
        String today = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        String tomorrow = String.valueOf(LocalDate.now().plusDays(1).format(DateTimeFormatter.ofPattern("yyyyMMdd")));
        String key1 = "visitor:" + today;
        String key2 = "visitor:" + tomorrow;

        redisTemplate.delete(Arrays.asList(key1, key2));
        redisTemplate.opsForHyperLogLog().delete("total_visitors_hll");
    }

    @Test
    @DisplayName("일일 방문자 수를 구할 수 있다")
    public void testGetTodayVisitorCount() {
        // Mock HttpServletRequest와 HttpServletResponse 객체 생성
        HttpServletRequest request = Mockito.mock(HttpServletRequest.class);
        HttpServletResponse response = Mockito.mock(HttpServletResponse.class);

        // trackVisitor 메서드 호출하여 방문자를 추가
        for (int i = 0; i < 5; i++) {
            visitorService.trackVisitor(request, response);
        }

        // 방문자 수 확인
        long todayVisitorCount = visitorService.getTodayVisitorCount();
        long totalVisitorCount = visitorService.getTotalVisitorCount();
        log.info("일일 방문자 수 : " + todayVisitorCount);
        log.info("총 방문자 수 : " + totalVisitorCount);

        assertEquals(5L, todayVisitorCount);
        assertEquals(5L, totalVisitorCount);
    }

    @Test
    @DisplayName("오늘, 내일 방문자를 더한 총 방문자 수를 구할 수 있다")
    public void testGetTotalVisitorCount() {
        // 오늘 방문자 추가
        for (int i = 0; i < 3; i++) {
            visitorService.trackVisitor(Mockito.mock(HttpServletRequest.class), Mockito.mock(HttpServletResponse.class));
        }

        // 내일 방문자 추가
        LocalDate tomorrow = LocalDate.now().plusDays(1);
        String tomorrowKey = "visitor:" + tomorrow.format(DateTimeFormatter.ofPattern("yyyyMMdd"));
        for (int i = 0; i < 5; i++) {
            redisTemplate.opsForSet().add(tomorrowKey, "visitor" + i);
            redisTemplate.opsForHyperLogLog().add("total_visitors_hll", "visitor" + i);
        }

        long totalVisitorCount = visitorService.getTotalVisitorCount();
        log.info("총 방문자 수 : " + totalVisitorCount);

        assertEquals(8L, totalVisitorCount); // 오늘 3명, 내일 5명 방문자이므로 총 8명이어야 함
    }
}