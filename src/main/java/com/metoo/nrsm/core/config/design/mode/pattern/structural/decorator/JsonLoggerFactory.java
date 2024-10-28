package com.metoo.nrsm.core.config.design.mode.pattern.structural.decorator;

import org.slf4j.LoggerFactory;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-29 16:38
 *
 * @title 装饰器模式（结构型模式）
 * 据日装饰器工厂，接收Logger
 */
public class JsonLoggerFactory {

    public static JsonLogger getLogger(Class<?> clazz){
        return new JsonLogger(LoggerFactory.getLogger(clazz));
    }

}
