package com.market.job.OrderCleanUp;

import com.market.domain.order.constant.OrderStatus;
import com.market.domain.order.service.OrderService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.JobScope;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

/**
 * 주문 상태 ORDER 인 주문 목록 하루에 한 번씩 삭제
 */
@Configuration
@RequiredArgsConstructor
public class OrderCleanUpConfig {

    private final OrderService orderService;

    @Bean
    public Job orderCleanupJob(JobRepository jobRepository, Step orderCleanupStep) {
        return new JobBuilder("orderCleanupJob", jobRepository)
            .incrementer(new RunIdIncrementer())
            .start(orderCleanupStep)
            .build();
    }

    @JobScope
    @Bean
    public Step orderCleanupStep(JobRepository jobRepository,
        PlatformTransactionManager transactionManager,
        Tasklet orderCleanupTasklet) {
        return new StepBuilder("orderCleanupStep", jobRepository)
            .tasklet(orderCleanupTasklet, transactionManager)
            .build();
    }

    @StepScope
    @Bean
    public Tasklet orderCleanupTasklet() {
        return (contribution, chunkContext) -> {
            orderService.deleteOrdersInBatches(OrderStatus.ORDER, 100);
            return RepeatStatus.FINISHED;
        };
    }
}