package com.metoo.nrsm.core.config.design.mode.pattern.creational.sinleton;


/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-09 11:38
 * @title 单例模式（结构型模式）
 */
public class Singleton {

    // 懒汉式
//    private static Singleton singleton;
    // 饿汉式
    private static Singleton singleton = new Singleton();

    private Singleton() {
    }

    private static class SingletonHolder {

        private static final Singleton INSTANCE = new Singleton();

    }

    public static final Singleton getInstance() {
        return SingletonHolder.INSTANCE;
    }

}
