package com.metoo.nrsm.core.config.utils.gather.strategy;

import io.swagger.annotations.ApiModel;
import lombok.Data;
import lombok.experimental.Accessors;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-23 15:45
 */
@ApiModel("")
@Data
@Accessors(chain = true)
public class Context<T> {

//    private Device device;

    private T entity;

    private Date addTime;

    private CountDownLatch latch;

    private String path;

    public Context() {
    }

    public Context(T entity, Date createTime, CountDownLatch latch) {
        this.entity = entity;
        this.addTime = addTime;
        this.latch = latch;
    }

    public Context(T entity, Date createTime, CountDownLatch latch, String path) {
        this.entity = entity;
        this.addTime = addTime;
        this.latch = latch;
        this.path = path;
    }
}
