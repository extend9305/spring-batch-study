package com.ds.spring_batch.sample.config.querydsl;

import com.ds.spring_batch.sample.config.jpa.CustomerItemProcessor;
import com.ds.spring_batch.sample.model.Customer;
import com.ds.spring_batch.sample.model.QCustomer;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.ItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;

@Slf4j
@Configuration
public class QueryDSLPagingReaderJobConfig {
    /**
     * CHUNK 크기를 지정한다.
     */
    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String QUERYDSL_PAGING_CHUNK_JOB = "QUERYDSL_PAGING_CHUNK_JOB";

    @Autowired
    DataSource dataSource;

    @Autowired
    EntityManagerFactory entityManagerFactory;

    @Bean
    public Job customerQuerydslPagingJob(Step customerQuerydslPagingStep, JobRepository repository, JobRepository jobRepository) throws Exception {
        log.info("-------------- Init customerJpaPagingJob --------------");
        return new JobBuilder(QUERYDSL_PAGING_CHUNK_JOB,jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(customerQuerydslPagingStep)
                .build();
    }

    @Bean
    public Step customerQuerydslPagingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) {
        log.info("-------------- Init customerQuerydslPagingStep --------------");
        return new StepBuilder("customerQuerydslPagingStep",jobRepository)
                .<Customer,Customer>chunk(CHUNK_SIZE,transactionManager)
                .reader(customerQuerydslPagingItemReader())
                .processor(new CustomerItemProcessor())
                .writer(customerQuerydslFlatFileItemWriter())
                .build();
    }

    @Bean
    public QuerydslPagingItemReader<Customer> customerQuerydslPagingItemReader() {
        return new QuerydslPagingItemReaderBuilder<Customer>()
                .name("customerQuerydslPagingItemReader")
                .entityManagerFactory(entityManagerFactory)
                .chunkSize(2)
                .querySupplier(jpaQueryFactory -> jpaQueryFactory.select(QCustomer.customer).from(QCustomer.customer).where(QCustomer.customer.age.gt(20)))
                .build();
    }
    @Bean
    public ItemWriter<? super Customer> customerQuerydslFlatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerQuerydslFlatFileItemWriter")
                .resource(new FileSystemResource("./customer_new_v2.csv"))
                .encoding(ENCODING)
                .delimited().delimiter("\t")
                .names("Name", "Age", "Gender")
                .build();
    }

}
