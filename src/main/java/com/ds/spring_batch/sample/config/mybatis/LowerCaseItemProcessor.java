package com.ds.spring_batch.sample.config.mybatis;

import com.ds.spring_batch.sample.model.Customer;
import org.springframework.batch.item.ItemProcessor;

public class LowerCaseItemProcessor implements ItemProcessor <Customer,Customer>{

    @Override
    public Customer process(Customer item) throws Exception {
        item.setName(item.getName().toLowerCase());
        item.setGender(item.getGender().toLowerCase());

        return item;
    }
}
