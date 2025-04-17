package com.ds.spring_batch.sample.config.mybatis;

import com.ds.spring_batch.sample.model.Customer;
import org.springframework.batch.item.ItemProcessor;


public class After20YearsItemProcessor implements ItemProcessor<Customer,Customer> {
    @Override
    public Customer process(Customer item) throws Exception {
        item.setAge(item.getAge() + 20);
        return item;
    }
}
