package com.ds.spring_batch.sample.config.jdbc;

import com.ds.spring_batch.sample.config.common.BatchJobAndStepListener;
import com.ds.spring_batch.sample.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.job.builder.JobBuilder;
import org.springframework.batch.core.launch.support.RunIdIncrementer;
import org.springframework.batch.core.repository.JobRepository;
import org.springframework.batch.core.step.builder.StepBuilder;
import org.springframework.batch.item.database.JdbcPagingItemReader;
import org.springframework.batch.item.database.Order;
import org.springframework.batch.item.database.PagingQueryProvider;
import org.springframework.batch.item.database.builder.JdbcPagingItemReaderBuilder;
import org.springframework.batch.item.database.support.SqlPagingQueryProviderFactoryBean;
import org.springframework.batch.item.file.FlatFileItemWriter;
import org.springframework.batch.item.file.builder.FlatFileItemWriterBuilder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.FileSystemResource;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.transaction.PlatformTransactionManager;

import javax.sql.DataSource;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Configuration
public class JdbcPagingReaderJobConfig {
    //CHUNK 크기를 지정한다.
    public static final int CHUNK_SIZE = 30;
    public static final String ENCODING = "UTF-8";
    public static final String JDBC_PAGING_CHUNK_JOB = "JDBC_PAGING_CHUNK_JOB";

    @Autowired
    private DataSource dataSource;

    @Bean
    public PagingQueryProvider queryProvider() throws Exception { // PagingQueryProvider : 데이터베이스에서 페이징 처리된 쿼리를 생성하는 역할
        //SqlPagingQueryProviderFactoryBean : 일반적인 팩토리 클래스로, 데이터베이스 타입에 따라 적절한 PagingQueryProvider 구현체를 생성해
        SqlPagingQueryProviderFactoryBean queryProvider = new SqlPagingQueryProviderFactoryBean();
        queryProvider.setDataSource(dataSource); //데이터 소스 설정
        queryProvider.setSelectClause("id, name, age, gender"); //select 할 컬럼명 지정
        queryProvider.setFromClause("from customer"); //테이블 조회
        queryProvider.setWhereClause("where age >= :age"); //조건절

        Map<String, Order> sortKeys = new HashMap<>(1);
        sortKeys.put("id", Order.DESCENDING);

        queryProvider.setSortKeys(sortKeys);

        return queryProvider.getObject();
    }



    @Bean
    public JdbcPagingItemReader<Customer> jdbcPagingItemReader() throws Exception {
        Map<String, Object> parameterValue = new HashMap<>(); //파라미터로 사용할 값을 저장할 맵 생성
        parameterValue.put("age", 20); //age 컬럼에 20 셋팅

        return new JdbcPagingItemReaderBuilder<Customer>()
                .name("jdbcPagingItemReader") //아이템리더 이름 설정
                .fetchSize(CHUNK_SIZE) //한번에 읽어올 데이터 사이즈 설정
                .pageSize(CHUNK_SIZE)
                .dataSource(dataSource) // db와 연결 정보 셋팅
                .rowMapper(new BeanPropertyRowMapper<>(Customer.class)) //DB에서 읽어온 ResultSet을 Customer 객케로 변환할때 사용할 맵퍼 설정
                .queryProvider(queryProvider()) //db별 적합한 페이징 쿼리 생성
                .parameterValues(parameterValue) //쿼리에서 사용할 파라미터 전달 (조건 age=20 적용)
                .build();
    }


    @Bean
    public FlatFileItemWriter<Customer> customerFlatFileItemWriter() {
        return new FlatFileItemWriterBuilder<Customer>()
                .name("customerFlatFileItemWriter")
                .resource(new FileSystemResource("./customer_new_v1.csv"))
                .encoding(ENCODING)
                .delimited().delimiter("\t")
                .names("Name", "Age", "Gender")
                .build();
    }


    @Bean
    public Step customerJdbcPagingStep(JobRepository jobRepository, PlatformTransactionManager transactionManager) throws Exception {
        log.info("------------------ Init customerJdbcPagingStep -----------------");

        return new StepBuilder("customerJdbcPagingStep", jobRepository)
                .<Customer, Customer>chunk(CHUNK_SIZE, transactionManager)
                .reader(jdbcPagingItemReader())
                .listener(new BatchJobAndStepListener())
                .writer(customerFlatFileItemWriter())
                .build();
    }

    @Bean
    public Job customerJdbcPagingJob(Step customerJdbcPagingStep, JobRepository jobRepository) {
        log.info("------------------ Init customerJdbcPagingJob -----------------");
        return new JobBuilder(JDBC_PAGING_CHUNK_JOB, jobRepository)
                .incrementer(new RunIdIncrementer())
                .listener(new BatchJobAndStepListener())
                .start(customerJdbcPagingStep)
                .build();
    }
}
