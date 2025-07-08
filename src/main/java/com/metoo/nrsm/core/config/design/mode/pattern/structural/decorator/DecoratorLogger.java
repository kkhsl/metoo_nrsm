package com.metoo.nrsm.core.config.design.mode.pattern.structural.decorator;

import org.slf4j.Logger;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-29 16:28
 * @title 装饰器模式（结构型模式）
 * <p>
 * 抽象装饰器
 * <p>
 * 使用场景：
 */
public abstract class DecoratorLogger implements Logger {

    protected Logger logger;

    public DecoratorLogger(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void info(String s) {
    }

    @Override
    public void error(String s) {
    }
}
