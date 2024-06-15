package com.metoo.nrsm.core.config.design.mode.pattern.creational.prototype;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 16:21
 */
public class Circle extends Shape {

    public Circle(){
        type = "Circle";
    }

    @Override
    public void draw() {
        System.out.println("Inside Circle::draw() method.");
    }
}
