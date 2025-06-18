package com.metoo.nrsm.core.config.design.mode.pattern.structural.decorator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-29 16:37
 *
 * @title 装饰器模式（结构型模式）
 *
 */
public class TestDeCorator {

    private static final Logger logger = LoggerFactory.getLogger(TestDeCorator.class);

    private static final Logger jsonLogger = JsonLoggerFactory.getLogger(TestDeCorator.class);

    public static void main(String[] args) {

        logger.info(" logger info 日志打印....");
        jsonLogger.info(" jsonLogger info 日志打印....");


        logger.error("logger error日志打印....");
        jsonLogger.error("jsonLogger error日志打印....");

    }
}
