package com.ds.spring_batch.sample.config.mybatis;

import com.ds.spring_batch.sample.model.Customer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class CustomService {
    public Map<String,String> processToOtherService(Customer item){
        log.info("call processToOtherService ..");
        return Map.of("code","200","message","ok");
    }
}
