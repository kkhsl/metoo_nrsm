package com.metoo.nrsm.core.config.design.mode.pattern.creational.prototype;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-10 16:23
 */
public class PrototypePatternDemo {

    public static void main(String[] args) {

        ShapeCache.loadCache();

        Shape clonedShape = (Shape) ShapeCache.getShape("1");
        System.out.println("Shape : " + clonedShape.getType());

        Shape clonedShape2 = (Shape) ShapeCache.getShape("2");
        System.out.println("Shape : " + clonedShape2.getType());

        Shape clonedShape3 = (Shape) ShapeCache.getShape("3");
        System.out.println("Shape : " + clonedShape3.getType());
    }
}
