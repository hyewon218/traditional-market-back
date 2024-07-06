package com.market.global.redis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.market.domain.item.dto.ItemTop5ResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.Conditions;
import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.repository.configuration.EnableRedisRepositories;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

import java.util.List;

@Slf4j
@Configuration
@EnableRedisRepositories
@EnableCaching
@RequiredArgsConstructor
public class RedisConfig {

    private final RedisProperties redisProperties;
    private final ObjectMapper objectMapper;

    @Bean
    public ModelMapper modelMapper() {
        ModelMapper modelMapper = new ModelMapper();
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());
        modelMapper.getConfiguration().setAmbiguityIgnored(true);
        modelMapper.getConfiguration().setFieldMatchingEnabled(true);
        modelMapper.getConfiguration().setFieldAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        modelMapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
        modelMapper.getConfiguration().setMethodAccessLevel(org.modelmapper.config.Configuration.AccessLevel.PRIVATE);
        modelMapper.getConfiguration().setFullTypeMatchingRequired(true);
        modelMapper.getConfiguration().setPropertyCondition(Conditions.isNotNull());

        return modelMapper;
    }

    @Bean
    public RedisConnectionFactory redisConnectionFactory() {
        return new LettuceConnectionFactory(redisProperties.getHost(), redisProperties.getPort());
    }

    @Bean
    @Primary
    public RedisTemplate<String, String> redisTemplate() {
        final RedisTemplate<String, String> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new StringRedisSerializer());
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

    @Bean
    public RedisTemplate<String, List<ItemTop5ResponseDto>> rankRedisTemplate() {
        final RedisTemplate<String, List<ItemTop5ResponseDto>> template = new RedisTemplate<>();
        template.setKeySerializer(new StringRedisSerializer());
        // Value Serializer 설정 (JSON Serializer)
        Jackson2JsonRedisSerializer<List<ItemTop5ResponseDto>> jsonSerializer = new Jackson2JsonRedisSerializer(List.class);
        jsonSerializer.setObjectMapper(objectMapper);
        template.setValueSerializer(jsonSerializer);
        template.setConnectionFactory(redisConnectionFactory());
        return template;
    }

}
