package com.metoo.nrsm.core.utils.random;

import java.util.Random;

public class RandomRangeUtils {

    public static double getRandomRange() {
        Random random = new Random();

        int number = 1;
        // 定义范围 避免数值均匀分布在35-85之间
        double[][] ranges = {
                {35, 45},
                {40, 50},
                {45, 55},
                {50, 60},
                {55, 65},
                {60, 70},
                {65, 75},
                {35, 65},
                {35, 55},
                {35, 70},
                {35, 75},
                {35, 80},
                {40, 65},
                {40, 75},
                {40, 85},
                {45, 65},
                {45, 75},
                {45, 85}
        };

        // 存储生成的随机数
        double[] randomValues = new double[number];

        // 生成十个随机数
        for (int i = 0; i < number; i++) {
            // 随机选择一个范围
            double[] range = ranges[random.nextInt(ranges.length)];
            randomValues[i] = range[0] + (range[1] - range[0]) * random.nextDouble();
        }

        // 打印生成的随机数
//        System.out.println("Generated random values:");
//        for (double value : randomValues) {
//            System.out.println(value);
//        }

        // 从生成的随机数中随机选择一个
        double selectedValue = randomValues[random.nextInt(randomValues.length)];
//        System.out.println("Randomly selected value from generated numbers: " + selectedValue);
        return selectedValue / 100;
    }

    public static void main(String[] args) {
        double randomNumber = getRandomRange();
        System.out.println(randomNumber);
    }
}
