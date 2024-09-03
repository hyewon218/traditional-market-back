package com.market.global.redis;

import java.time.Duration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import com.fasterxml.jackson.databind.jsontype.BasicPolymorphicTypeValidator;
import com.fasterxml.jackson.databind.module.SimpleModule;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableCaching // Spring Boot 의 캐싱 설정을 활성화
public class RedisCacheConfig {
    @Bean(name = "ItemTop5CacheManager")
    @Primary
    public CacheManager ItemTop5CacheManager(RedisConnectionFactory redisConnectionFactory) {
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration
            .defaultCacheConfig()
            // Redis 에 Key 를 저장할 때 String 으로 직렬화(변환)해서 저장
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new StringRedisSerializer()))
            // Redis 에 Value 를 저장할 때 Json 으로 직렬화(변환)해서 저장
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(
                    new Jackson2JsonRedisSerializer<>(Object.class)
                )
            )
            // 데이터의 만료기간(TTL) 설정
            .entryTtl(Duration.ofDays(1));

        return RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory)
            .cacheDefaults(redisCacheConfiguration)
            .build();
    }

    @Bean(name = "marketCacheManager")
    public CacheManager marketCacheManager(RedisConnectionFactory redisConnectionFactory) {
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule()); // Java 8 날짜/시간 API 지원 모듈 등록
        objectMapper.activateDefaultTyping( // ClassCastException 오류 해결
            BasicPolymorphicTypeValidator.builder()
                .allowIfBaseType(Object.class)
                .build(),
            DefaultTyping.NON_FINAL
        );

        SimpleModule module = new SimpleModule();
        module.addSerializer(PageImpl.class, new PageSerializer());
        module.addDeserializer(PageImpl.class, new PageDeserializer());
        objectMapper.registerModule(module);

        // GenericJackson2JsonRedisSerializer를 생성자의 ObjectMapper로 초기화
        // 모든 클래스 타입의 정보 저장 (커스텀 ObjectMapper 사용한 경우엔 예외, 직렬화/역직렬화 시 클래스타입 정보 포함 안하기때문)
        GenericJackson2JsonRedisSerializer genericJackson2JsonRedisSerializer =
            new GenericJackson2JsonRedisSerializer(objectMapper);

        // RedisCacheConfiguration에 커스터마이징된 직렬화 설정 적용
        RedisCacheConfiguration redisCacheConfiguration = RedisCacheConfiguration
            .defaultCacheConfig()
            .serializeKeysWith(
                RedisSerializationContext.SerializationPair.fromSerializer(new StringRedisSerializer()))
            .serializeValuesWith(
                RedisSerializationContext.SerializationPair.fromSerializer(genericJackson2JsonRedisSerializer))
            .entryTtl(Duration.ofDays(1));

        return RedisCacheManager
            .RedisCacheManagerBuilder
            .fromConnectionFactory(redisConnectionFactory)
            .cacheDefaults(redisCacheConfiguration)
            .build();
    }
}