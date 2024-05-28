package com.market.domain.notification.repository;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Repository
@RequiredArgsConstructor
public class EmitterRepository {

    private final Map<String, SseEmitter> emitterMap = new HashMap<>();

    public SseEmitter save(Long memberNo, SseEmitter emitter) { // 알람 받는 memberNo
        final String key = getKey(memberNo);
        emitterMap.put(key, emitter); // key = emitter:UID:1
        return emitter;
    }

    public void delete(Long memberNo) {
        emitterMap.remove(getKey(memberNo));
    }

    public Optional<SseEmitter> get(Long memberNo) {
        SseEmitter result = emitterMap.get(getKey(memberNo));
        return Optional.ofNullable(result);
    }

    private String getKey(Long memberNo) {
        return "emitter:UID:" + memberNo;
    }
}