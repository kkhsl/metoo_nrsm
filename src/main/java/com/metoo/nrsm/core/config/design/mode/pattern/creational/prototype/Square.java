package com.metoo.nrsm.core.config.design.mode.pattern.creational.prototype;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 16:22
 */
public class Square extends Shape {

    public Square() {
        type = "Square";
    }

    @Override
    public void draw() {
        System.out.println("Inside Square::draw() method.");
    }

}
