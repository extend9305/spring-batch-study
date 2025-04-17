package com.ds.spring_batch.sample.config.flatfilereader.jdbc;

import com.ds.spring_batch.sample.model.Customer;
import org.springframework.batch.item.database.ItemSqlParameterSourceProvider;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;

// Customer 객체를 SQL 파라미터로 변환하는 ItemSqlParameterSourceProvider 구현 클래스
public class CustomerItemSqlParameterSourceProvider implements ItemSqlParameterSourceProvider<Customer> {
    //SQL 파라미터를 생성하는 메서드의 구현
    @Override
    public SqlParameterSource createSqlParameterSource(Customer item) {
        // 주어진 Customer 객체의 속성을 SQL 파라미터로 변환하기 위해 BeanPropertySqlParameterSource 사용.
        return new BeanPropertySqlParameterSource(item);
    }
}
