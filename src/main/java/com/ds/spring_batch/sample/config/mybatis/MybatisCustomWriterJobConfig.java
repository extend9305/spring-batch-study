package com.ds.spring_batch.sample.config.mybatis;

import com.ds.spring_batch.sample.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
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
public class MybatisCustomWriterJobConfig {
    /**
     * CHUNK 크기를 지정한다.
     */
    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String MYBATIS_CHUNK_JOB = "MYBATIS_CUSTOM_CHUNK_JOB";

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Autowired
    CustomItemWriter customItemWriter;

    @Bean
    public Job mybatisCustomWriteJob(Step mybatisCustomWriteStep, JobRepository jobRepository) {
        log.info("----------------- Init mybatisCustomWriteJob -----------------");
        return new JobBuilder(MYBATIS_CHUNK_JOB,jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(mybatisCustomWriteStep)
                .build();
    }

    @Bean
    public Step mybatisCustomWriteStep(JobRepository jobRepository, PlatformTransactionManager transactionManager, FlatFileItemReader<Customer> flatFileItemReader) {
        log.info("----------------- Init mybatisCustomWriteStep -----------------");
        return new StepBuilder("mybatisCustomWriteStep",jobRepository)
                .<Customer,Customer>chunk(CHUNK_SIZE,transactionManager)
                .reader(mybatisCustomReader())
                .writer(customItemWriter)
                .build();
    }

    @Bean
    public FlatFileItemReader<Customer> mybatisCustomReader() {

        return new FlatFileItemReaderBuilder<Customer>()
                .name("mybatisCustomReader")
                .resource(new ClassPathResource("./customer.csv"))
                .encoding(ENCODING)
                .delimited().delimiter(",")
                .names("name", "age", "gender")
                .targetType(Customer.class)
                .build();
    }
}
