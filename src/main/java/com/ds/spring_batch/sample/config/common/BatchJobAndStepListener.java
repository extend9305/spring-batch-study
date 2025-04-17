package com.ds.spring_batch.sample.config.common;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.JobExecutionListener;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.StepExecutionListener;

@Slf4j
public class BatchJobAndStepListener implements JobExecutionListener , StepExecutionListener {
    // Job 시작 전
    @Override
    public void beforeJob(JobExecution jobExecution) {
        log.info(">>>>> [Job Start] JobName: {}, JobParameters: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getJobParameters());
    }

    // Job 종료 후
    @Override
    public void afterJob(JobExecution jobExecution) {
        log.info("<<<<< [Job End] JobName: {}, Status: {}",
                jobExecution.getJobInstance().getJobName(),
                jobExecution.getStatus());
    }

    // Step 시작 전
    @Override
    public void beforeStep(StepExecution stepExecution) {
        log.info(">>>>> [Step Start] StepName: {}, ReadCount: {}",
                stepExecution.getStepName(),
                stepExecution.getReadCount());
    }

    // Step 종료 후
    @Override
    public ExitStatus afterStep(StepExecution stepExecution) {
        log.info("<<<<< [Step End] StepName: {}, Status: {}, ReadCount: {}, WriteCount: {}",
                stepExecution.getStepName(),
                stepExecution.getStatus(),
                stepExecution.getReadCount(),
                stepExecution.getWriteCount());
        return stepExecution.getExitStatus();
    }
}
