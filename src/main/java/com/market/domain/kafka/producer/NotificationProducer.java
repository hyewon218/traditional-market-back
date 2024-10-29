package com.market.domain.kafka.producer;

import com.market.domain.notification.entity.NotificationEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationProducer {

    private final KafkaTemplate<Long, NotificationEvent> alarmEventKafkaTemplate;

    @Value("${spring.kafka.topic.notification}")
    private String topic;

    public void send(NotificationEvent event) {
        log.info("send start");
        alarmEventKafkaTemplate.send(topic, event.getReceiverNo(), event);
        log.info("send fin");
    }
}