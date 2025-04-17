package com.ds.spring_batch.sample.config.jpa;

import com.ds.spring_batch.sample.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemProcessor;

@Slf4j
public class CustomerItemProcessor implements ItemProcessor<Customer, Customer> {
    @Override
    public Customer process(Customer item) throws Exception {
        log.info("CustomerItemProcessor process called {}" , item);
        return item;
    }
}
