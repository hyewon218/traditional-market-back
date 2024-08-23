package com.market.scheduler;

import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.JobParameters;
import org.springframework.batch.core.JobParametersBuilder;
import org.springframework.batch.core.JobParametersInvalidException;
import org.springframework.batch.core.launch.JobLauncher;
import org.springframework.batch.core.repository.JobExecutionAlreadyRunningException;
import org.springframework.batch.core.repository.JobInstanceAlreadyCompleteException;
import org.springframework.batch.core.repository.JobRestartException;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class Scheduler {

    private final Job orderCleanupJob;
    private final JobLauncher jobLauncher;

    //@Scheduled(cron = "0 */1 * * * *") // 1분마다
    @Scheduled(cron = "0 0 4 * * *") // 하루에 한번
    public void orderCleanupJobRun()
        throws JobInstanceAlreadyCompleteException,
        JobExecutionAlreadyRunningException,
        JobParametersInvalidException,
        JobRestartException {

        JobParameters jobParameters = new JobParametersBuilder()
            .addLong("requestTime", Instant.now().toEpochMilli())
            .toJobParameters();

        jobLauncher.run(orderCleanupJob, jobParameters);
    }
}