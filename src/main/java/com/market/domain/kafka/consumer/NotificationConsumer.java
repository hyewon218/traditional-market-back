package com.market.domain.kafka.consumer;

import com.market.domain.notification.entity.NotificationEvent;
import com.market.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class NotificationConsumer {

    private final NotificationService alarmService;

    @KafkaListener(
        topics = "${spring.kafka.topic.notification}", // topics 속성을 사용하여 리스너가 메시지를 소비해야 하는 Kafka 주제를 지정할 수 있다. 리스너는 지정된 주제를 자동으로 구독한다.
        groupId = "notification", // groupId 속성은 리스너가 속한 소비자 그룹을 지정하는 데 사용된다. 동일한 그룹 ID를 가진 Kafka 소비자는 구독 주제의 메시지 처리 작업을 공유하여 병렬성과 로드 밸런싱을 제공한다.
        containerFactory = "kafkaListenerContainerFactory") // containerFactory 속성을 사용하면 기본 Kafka 메시지 리스너 컨테이너를 생성하는 데 사용되는 Bean 의 이름을 지정할 수 있다. 이를 통해 동시성, 승인 모드, 오류 처리 등 컨테이너의 다양한 속성을 유연하게 사용자 지정할 수 있다.
    public void consumeNotification(ConsumerRecord<Long, NotificationEvent> record, Acknowledgment ack) {
        try {
            // 메시지 값 추출
            NotificationEvent event = record.value();
            log.info("Consume the event {}", event);

            alarmService.send(
                event.getType(),
                event.getArgs(),
                event.getReceiverNo()
            );

            // 처리 완료 후 명시적(수동) 오프셋 커밋
            ack.acknowledge();
            log.info("Acknowledged the event {}", event);

        } catch (Exception e) {
            log.error("Error processing notification event", e);
        }
    }
}