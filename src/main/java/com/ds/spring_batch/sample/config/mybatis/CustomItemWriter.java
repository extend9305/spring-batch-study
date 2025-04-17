package com.ds.spring_batch.sample.config.mybatis;

import com.ds.spring_batch.sample.model.Customer;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class CustomItemWriter implements ItemWriter<Customer> {
    private final CustomService customService;

    @Override
    public void write(Chunk<? extends Customer> chunk) throws Exception {
        for (Customer customer: chunk) {
            log.info("Call Porcess in CustomItemWriter...");
            customService.processToOtherService(customer);
        }
    }
}
