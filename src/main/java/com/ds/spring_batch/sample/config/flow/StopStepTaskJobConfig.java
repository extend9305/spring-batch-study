package com.ds.spring_batch.sample.config.flow;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.transaction.PlatformTransactionManager;

import java.util.Random;

@Slf4j
@Configuration
public class StopStepTaskJobConfig {

    public static final String STOP_STEP_TASK = "STOP_STEP_TASK";

    @Autowired
    PlatformTransactionManager transactionManager;

    @Bean(name = "stepStop01")
    public Step stepStop01(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("------------------ Init myStep -----------------");

        return new StepBuilder("stepStop01", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("Execute Step 01 Tasklet ...");

                        Random random = new Random();
                        int randomValue = random.nextInt(1000);

                        if (randomValue % 2 == 0) {
                            return RepeatStatus.FINISHED;
                        } else {
                            throw new RuntimeException("Error This value is Odd: " + randomValue);
                        }
                    }
                }, transactionManager)
                .build();
    }

    @Bean(name = "stepStop02")
    public Step stepStop02(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("------------------ Init myStep -----------------");

        return new StepBuilder("stepStop02", jobRepository)
                .tasklet(new Tasklet() {
                    @Override
                    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
                        log.info("Execute Step 02 Tasklet ...");
                        return RepeatStatus.FINISHED;
                    }
                }, transactionManager)
                .build();
    }

    @Bean
    public Job stopStepJob(Step stepStop01, Step stepStop02, JobRepository jobRepository) {
        log.info("------------------ Init myJob -----------------");
        return new JobBuilder(STOP_STEP_TASK, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(stepStop01)
                .on("FAILED").stop()
                .from(stepStop01).on("COMPLETED").to(stepStop02)
                .end()
                .build();
    }

}
