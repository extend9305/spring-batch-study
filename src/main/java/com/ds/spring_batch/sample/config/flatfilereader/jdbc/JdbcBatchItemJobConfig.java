package com.ds.spring_batch.sample.config.flatfilereader.jdbc;

import com.ds.spring_batch.sample.config.common.BatchJobAndStepListener;
import com.ds.spring_batch.sample.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcBatchItemWriter;
import org.springframework.batch.item.database.builder.JdbcBatchItemWriterBuilder;
import org.springframework.batch.item.file.FlatFileItemReader;
import org.springframework.batch.item.file.builder.FlatFileItemReaderBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.ClassPathResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class JdbcBatchItemJobConfig {
    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String JDBC_BATCH_WRITER_CHUNK_JOB = "JDBC_JOB";

    @Autowired
    DataSource dataSource;


    @Bean
    public Job flatFileJob(Step flatFileStep, JobRepository jobRepository) {
        log.info("------------------ Init flatFileJob -----------------");
        return new JobBuilder(JDBC_BATCH_WRITER_CHUNK_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new BatchJobAndStepListener())
                .start(flatFileStep)
                .build();
    }

    @Bean
    public Step flatFileStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("------------------ Init flatFileStep -----------------");

        return new StepBuilder("flatFileStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .listener(new BatchJobAndStepListener())
                .reader(flatFileItemReader())
                .writer(flatFileItemWriter())
                .build();
    }

    @Bean
    public FlatFileItemReader<Customer> flatFileItemReader() {
        return new FlatFileItemReaderBuilder<Customer>()
                .name("FlatFileItemReader")
                .resource(new ClassPathResource("./customer.csv")) //github 통한 파일 다운로드
                .encoding(ENCODING)
                .delimited().delimiter(",")
                .names("name", "age", "gender")
                .targetType(Customer.class)
                .build();
    }

    @Bean
    public JdbcBatchItemWriter<Customer> flatFileItemWriter() {
        return new JdbcBatchItemWriterBuilder<Customer>()
                .dataSource(dataSource) // // 1. 사용할 데이터소스를 설정 (DB 연결 정보)
                .sql("INSERT INTO customer (name, age, gender) VALUES (:name, :age, :gender)") // 2. 실행할 SQL 쿼리를 설정 (Customer 객체의 필드를 삽입하는 INSERT 쿼리)
                .itemSqlParameterSourceProvider(new CustomerItemSqlParameterSourceProvider()) // 3. SQL 파라미터 소스를 제공하는 클래스를 설정 (Customer 객체의 속성을 SQL 파라미터로 변환)
                .build();
    }




}
