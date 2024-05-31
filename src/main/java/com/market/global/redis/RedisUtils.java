package com.market.global.redis;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, String> redisBlackListTemplate;
    private final ModelMapper modelMapper;

    public void setValues(String key, String value, Duration expiredAt) {
        redisTemplate.opsForValue().set(key, value, expiredAt.toMillis(), TimeUnit.MILLISECONDS);
    }

    public void setValues(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

//    public void setValues(String key, String value, Duration expiredAt) {
//        if (expiredAt != null) {
//            redisTemplate.opsForValue().set(key, value, expiredAt.toMillis(), TimeUnit.MILLISECONDS);
//        } else {
//            redisTemplate.opsForValue().set(key, value);
//        }
//    }

//    public <T> T getValues(String key, Class<T> clazz){
//    Object o = redisTemplate.opsForValue().get(key);
//        if(o != null) {
//            if(o instanceof LinkedHashMap<?,?>){
//                return modelMapper.map(o, clazz);
//            }else{
//                return clazz.cast(o);
//            }
//        }
//    return null;
//    }

    public String getValues(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public void setExpire(String key, long expireTime) {
        redisTemplate.expire(key, expireTime, TimeUnit.SECONDS);
    }

    public long getExpire(String key) {
        Long expireTime = redisTemplate.getExpire(key, TimeUnit.SECONDS);
        return (expireTime != null) ? expireTime : -1;
    }

    public void setBlackList(String key, String value, long expiredAt) {
        redisBlackListTemplate.opsForValue().set(key, "logout", expiredAt, TimeUnit.MILLISECONDS);
    }

//    public <T> T getBlackList(String key, Class<T> clazz){
//        Object o = redisBlackListTemplate.opsForValue().get(key);
//        if(o != null) {
//            if(o instanceof LinkedHashMap<?,?>){
//                return modelMapper.map(o, clazz);
//            }else{
//                return clazz.cast(o);
//            }
//        }
//        return null;
//    }

    public String getBlackListValue(String key) {
        return redisBlackListTemplate.opsForValue().get(key);
    }

//    public boolean hasBlack(String key) {
//        return getBlackListValue(key).startsWith("black");
//    }

    public boolean hasBlack(String key) {
        return getBlackListValue(key).equals("logout");
    }

    public void deleteValues(String key) {
        redisTemplate.delete(key);
    }

    public void deleteBlackList(String key) {
        redisBlackListTemplate.delete(key);
    }

    public boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }

    public boolean hasKeyBlackList(String key) {
        return redisBlackListTemplate.hasKey(key);
    }
}
