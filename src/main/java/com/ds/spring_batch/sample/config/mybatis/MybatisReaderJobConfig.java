package com.ds.spring_batch.sample.config.mybatis;

import com.ds.spring_batch.sample.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.batch.MyBatisBatchItemWriter;
import org.mybatis.spring.batch.MyBatisPagingItemReader;
import org.mybatis.spring.batch.builder.MyBatisBatchItemWriterBuilder;
import org.mybatis.spring.batch.builder.MyBatisPagingItemReaderBuilder;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.batch.item.support.CompositeItemProcessor;
import org.springframework.batch.item.support.builder.CompositeItemProcessorBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Configuration
public class MybatisReaderJobConfig {
    /**
     * CHUNK 크기를 지정한다.
     */
    public static final int CHUNK_SIZE = 2;
    public static final String ENCODING = "UTF-8";
    public static final String MYBATIS_CHUNK_JOB = "MYBATIS_CHUNK_JOB";

    @Autowired
    DataSource dataSource;

    @Autowired
    SqlSessionFactory sqlSessionFactory;

    @Bean
    public Job customerMybatisPagingJob(Step customerMybatisStep, JobRepository jobRepository) {
        log.info("------------------ Init customerMybatisPagingJob -----------------");
        return new JobBuilder(MYBATIS_CHUNK_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .start(customerMybatisStep)
                .build();
    }

    @Bean
    public Step customerMybatisStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        log.info("------------------ Init customerMybatisStep -----------------");

        return new StepBuilder("customerMybatisStep", jobRepository)
                .<Customer, Customer> chunk(CHUNK_SIZE, transactionManager)
                .reader(myBatisItemReader())
//                .processor(item -> {
//                    log.info("==================" + item.getName() + "==================");
//                    log.info("Before Age: " + item.getAge());
//                    log.info("After Age: " + item.getAge());
//                    return item;
//                })
                .processor(compositeItemProcessor())
                .writer(myBatisItemWriter())
                .build();
    }

    @Bean
    public FlatFileItemWriter<Customer> customerCursorFlatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerCursorFlatFileItemWriter")
                .resource(new FileSystemResource("./customer_new_v4.csv"))
                .encoding(ENCODING)
                .delimited().delimiter("\t")
                .names("Name", "Age", "Gender")
                .build();
    }


    @Bean
    public CompositeItemProcessor<Customer,Customer> compositeItemProcessor() {
        return new CompositeItemProcessorBuilder<Customer,Customer>()
                .delegates(List.of(
                    new LowerCaseItemProcessor(),
                    new After20YearsItemProcessor()
                ))
                .build();
    }

    @Bean
    public MyBatisPagingItemReader<Customer> myBatisItemReader() throws Exception {

        return new MyBatisPagingItemReaderBuilder<Customer>()
                .sqlSessionFactory(sqlSessionFactory)
                .pageSize(CHUNK_SIZE)
                .queryId("com.ds.spring_batch.sample.config.mybatis.CustomerMapper.selectCustomers")
                .build();
    }

    @Bean
    public MyBatisBatchItemWriter<Customer> myBatisItemWriter() {
        return new MyBatisBatchItemWriterBuilder<Customer>()
                .sqlSessionFactory(sqlSessionFactory)
                .statementId("com.ds.spring_batch.sample.config.mybatis.CustomerMapper.updateCustomer")
                .itemToParameterConverter(item -> {
                    Map<String, Object> parameter = new HashMap<>();
                    parameter.put("id", item.getId());
                    parameter.put("age", item.getAge());
                    parameter.put("gender", item.getGender());
                    return parameter;
                })
                .build();
    }


}
