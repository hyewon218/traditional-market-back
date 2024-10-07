package com.market.domain.kafka.config;

import com.market.domain.notification.entity.NotificationEvent;
import java.util.HashMap;
import java.util.Map;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.common.serialization.LongSerializer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.DefaultKafkaProducerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.core.ProducerFactory;
import org.springframework.kafka.support.serializer.JsonSerializer;

@Configuration
public class KafkaProducerConfig {
    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;
    /**
     * In-Sync-Replica 에 모두 event 가 저장되었음이 확인 되어야 ack 신호를 보냄.
     * 가장 성능은 떨어지지만 event produce 를 보장할 수 있음.
     */
    @Value("${spring.kafka.producer.acks}")
    private String acksConfig;
    @Bean
    public ProducerFactory<Long, NotificationEvent> producerFactory() {
        Map<String, Object> configProps = new HashMap<>();
        configProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        configProps.put(ProducerConfig.ACKS_CONFIG, acksConfig);
        configProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, LongSerializer.class);
        configProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, JsonSerializer.class);

        return new DefaultKafkaProducerFactory<>(configProps);
    }

    @Bean
    public KafkaTemplate<Long, NotificationEvent> alarmEventKafkaTemplate() {
        return new KafkaTemplate<>(producerFactory());
    }
}
