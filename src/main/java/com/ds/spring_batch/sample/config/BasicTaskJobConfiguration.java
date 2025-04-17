package com.ds.spring_batch.sample.config;

import com.ds.spring_batch.sample.jobs.task01.GreetingTask;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

@Slf4j
@Configuration
public class BasicTaskJobConfiguration {
    @Autowired
    PlatformTransactionManager transactionManager;

    @Bean
    public Tasklet greetingTasklet() {
        return new GreetingTask();
    }

    @Bean
    public Step step(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("------------------ Init myStep -----------------");

        return new StepBuilder("myStep",jobRepository)
                .<String,String>chunk(3,transactionManager)
                .reader(reader())
                .processor(processor())
                .writer(writer())
                .build();
    }

    @Bean
    public Job myJob(Step step, JobRepository jobRepository) {
        log.info("------------------ Init myJob -----------------");
        return new JobBuilder("myJob",jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(step)
                .build();
    }



    @Bean
    public ItemReader<String> reader() {
        return new FlatFileItemReaderBuilder<String>()
                .name("fileItemReader")
                .resource(new FileSystemResource("input.txt")) // 읽을 파일
                .lineMapper((line, lineNumber) -> {
                    log.info(line.toString());
                    return line;
                }) // 한 줄씩 그대로 읽음
                .build();
    }


    @Bean
    public ItemProcessor<String, String> processor() {
        return item ->{
            String processedItem = item.toUpperCase();
            log.info(">>> [PROCESS] 변환한 데이터: {} -> {}", item, processedItem);
            return processedItem;
        };
    }

    @Bean
    public ItemWriter<String> writer() {
        return new FlatFileItemWriterBuilder<String>()
                .name("fileItemWriter")
                .resource(new FileSystemResource("output.txt")) // 쓸 파일
                .lineAggregator(item -> item) // 한 줄씩 작성
                .build();
    }
}
