package com.ds.spring_batch.sample.config.common;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.lang.reflect.Array;

@Slf4j
@Aspect
@Configuration
public class BatchItemLoggingAspect {
    @Around("target(org.springframework.batch.item.ItemReader)")
    public Object logItemReader(ProceedingJoinPoint joinPoint) throws Throwable {
        Object result = joinPoint.proceed();
        if(result != null) {
            log.info(">>>>> [ItemReader] Read item: {}", result);
        }
        return result;
    }

    @Around("target(org.springframework.batch.item.ItemProcessor)")
    public Object logItemProcessor(ProceedingJoinPoint joinPoint) throws Throwable {
        Object input = joinPoint.getArgs()[0];
        log.info(">>>>> [ItemProcessor] Processing item: {}", input);
        Object result = joinPoint.proceed();
        log.info(">>>>> [ItemProcessor] Processed result: {}", result);
        return result;
    }

    @Around("target(org.springframework.batch.item.ItemWriter)")
    public Object logItemWriter(ProceedingJoinPoint joinPoint) throws Throwable {
        if(joinPoint.getArgs().length > 0) {
            Object input = joinPoint.getArgs()[0]; // List<?>
            log.info(">>>>> [ItemWriter] Writing items: {}", input);
        }
        return joinPoint.proceed();
    }
}
