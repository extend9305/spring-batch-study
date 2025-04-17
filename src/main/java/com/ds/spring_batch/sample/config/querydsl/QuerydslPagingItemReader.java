package com.ds.spring_batch.sample.config.querydsl;

import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import lombok.Builder;
import lombok.Data;
import org.springframework.batch.item.database.AbstractPagingItemReader;
import org.springframework.util.ClassUtils;
import org.springframework.util.CollectionUtils;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Function;

public class QuerydslPagingItemReader <T> extends AbstractPagingItemReader<T> {
    private EntityManager em;
    private final Function<JPAQueryFactory, JPAQuery<T>> querySupplier;
    private final Boolean alwaysReadFromZero;


    public QuerydslPagingItemReader(EntityManagerFactory entityManagerFactory, Function<JPAQueryFactory, JPAQuery<T>> querySupplier, int chunkSize) {
        this(ClassUtils.getShortName(QuerydslPagingItemReader.class), entityManagerFactory, querySupplier, chunkSize, false);
    }

    public QuerydslPagingItemReader(String name, EntityManagerFactory entityManagerFactory, Function<JPAQueryFactory, JPAQuery<T>> querySupplier, int chunkSize, Boolean alwaysReadFromZero) {
        super.setPageSize(chunkSize);
        setName(name);
        this.querySupplier = querySupplier;
        this.em = entityManagerFactory.createEntityManager();
        this.alwaysReadFromZero = alwaysReadFromZero;

    }

    @Override
    protected T doRead() throws Exception {
        return super.doRead();
    }

    @Override
    protected void doOpen() throws Exception {
        super.doOpen();
    }

    @Override
    protected void doClose() throws Exception {
        if (em != null) {
            em.close();
        }
        super.doClose();
    }

    @Override
    protected void jumpToItem(int itemIndex) throws Exception {
        super.jumpToItem(itemIndex);
    }

    @Override
    protected void doReadPage() {
        initQueryResult();

        JPAQueryFactory jpaQueryFactory = new JPAQueryFactory(em);
        long offset = 0;
        if(!alwaysReadFromZero) {
            offset = (long) getPage() * getPageSize();
        }

        JPAQuery<T> jpaQuery = querySupplier.apply(jpaQueryFactory).offset(offset).limit(getPageSize());

        List<T> quertResult = jpaQuery.fetch();
        for (T t : quertResult) {
            em.detach(t);
            results.add(t);
        }
    }

    private void initQueryResult(){
        if(CollectionUtils.isEmpty(results)){
            results = new CopyOnWriteArrayList<>();
        } else {
            results.clear();
        }
    }

}
