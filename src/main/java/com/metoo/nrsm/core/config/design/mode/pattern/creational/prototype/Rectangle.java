package com.metoo.nrsm.core.config.design.mode.pattern.creational.prototype;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 16:21
 */
public class Rectangle extends Shape {

    public Rectangle(){
        type = "Rectangle";
    }

    @Override
    void draw() {
        System.out.println("Inside Rectangle::draw() method.");
    }
}
