package com.market;

import java.util.concurrent.Executor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@EnableScheduling
@EnableJpaAuditing
@EnableAsync
@SpringBootApplication
public class TraditionalMarketBackApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraditionalMarketBackApplication.class, args);
    }

    @Bean
    public Executor asyncExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
       /* executor.setCorePoolSize(5); // 기본적으로 실행을 대기하고 있는 스레드 개수
        executor.setMaxPoolSize(10); // 동시 동작하는 최대 스레드 개수
        executor.setQueueCapacity(25); // MaxPoolSize 초과 요청에서 Thread 생성 요청시, 해당 요청을 Queue 에 저장하는데 이때 최대 수용 가능한 Queue 의 크기*/
        executor.setCorePoolSize(4);
        executor.setMaxPoolSize(8);
        executor.setQueueCapacity(10);
        executor.setThreadNamePrefix("AsyncThread-"); // 생성되는 스레드의 접두사 지정
        executor.initialize(); // 스레드풀 초기화 및 실행
        return executor;
    }
}