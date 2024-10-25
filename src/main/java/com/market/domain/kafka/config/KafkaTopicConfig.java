package com.market.domain.kafka.config;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KafkaTopicConfig {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.topic.notification}")
    private String topicName;

    @Value("${spring.kafka.topic.min.insync.replicas}")
    private String minInSyncReplicas;

    @Value("${spring.kafka.topic.partitions}")
    private int partitions;

    @Value("${spring.kafka.topic.replication-factor}")
    private short replicationFactor;

    @Bean
    public AdminClient adminClient() {
        Map<String, Object> config = new HashMap<>();
        config.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        return AdminClient.create(config);
    }

    @Bean
    public NewTopic createTopic() throws ExecutionException, InterruptedException {
        Map<String, String> topicConfigs = new HashMap<>();
        topicConfigs.put(TopicConfig.MIN_IN_SYNC_REPLICAS_CONFIG, minInSyncReplicas);

        NewTopic newTopic = new NewTopic(topicName, partitions, replicationFactor).configs(topicConfigs);

        adminClient().createTopics(Collections.singletonList(newTopic)).all().get(); // 주제 생성이 완료되었는지 확인

        return newTopic;
    }
}