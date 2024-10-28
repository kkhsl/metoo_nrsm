package com.metoo.nrsm.core.config.utils.gather.factory.gather.utils;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.utils.random.RandomRangeUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Random;

public class GeneraFlowUtils {

    /**
     * 计算表达式 随机数* v4 / (1 - 随机数)，并保留两位小数
     * @param v4 输入的 double 类型参数
     * @return 计算结果的字符串表示，保留两位小数
     */
    public static double generateV6(double v4, String random) {
        return method4(v4, random);
    }

    public static double generateV6(double v4) {
        return method1(v4);
    }

    public static double method1(double v4){
        Random random = new Random();
        double min = 0.36;
        double max = 0.85;


        // 生成指定范围内的随机数
        double randomNumber = min + (max - min) * random.nextDouble();

        // 计算表达式 xv4 / (1 - 随机数)
        double result = randomNumber * v4 / (1 - randomNumber);

        // 使用 BigDecimal 保留两位小数
        BigDecimal bd = BigDecimal.valueOf(result);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public static double method2(double v4){
        Random random = new Random();
        double min = generateRandomValueAbove(0.30, 0.49); // 0.30 - 0.49
        double max = generateRandomValueAbove(0.50, 0.69); // 0.50 - 0.69

        // 生成指定范围内的随机数
        double randomNumber = min + (max - min) * random.nextDouble();

        // 计算表达式 xv4 / (1 - 随机数)
        double result = randomNumber * v4 / (1 - randomNumber);

        // 使用 BigDecimal 保留两位小数
        BigDecimal bd = BigDecimal.valueOf(result);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }


    public static double method3(double v4){

        double randomNumber = RandomRangeUtils.getRandomRange();

        // 计算表达式 xv4 / (1 - 随机数)
        double result = randomNumber * v4 / (1 - randomNumber);

        // 使用 BigDecimal 保留两位小数
        BigDecimal bd = BigDecimal.valueOf(result);
        bd = bd.setScale(2, RoundingMode.HALF_UP);

        return bd.doubleValue();
    }

    public static double method4(double v4, String random){

        double randomNumber = 0;

        if(StringUtil.isNotEmpty(random)){
            String[] range = random.split(",");
            String min = range[0];
            String max = range[1];
            randomNumber = generateRandomValueAbove(Double.parseDouble(min), Double.parseDouble(max)) / 100;
        }

        // 计算表达式 xv4 / (1 - 随机数)
        double result = randomNumber * v4 / (1 - randomNumber);

        return result;

        // 使用 BigDecimal 保留两位小数
//        BigDecimal bd = BigDecimal.valueOf(result);
//        bd = bd.setScale(2, RoundingMode.HALF_UP);
//
//        return bd.doubleValue();
    }

    public static double generateRandomValueAbove(double lowerBound, double upperBound){

        // 确保生成的随机数大于 min
        Random random = new Random();
        double randomValue = lowerBound + (upperBound - lowerBound) * random.nextDouble();
        return randomValue;

    }

    public static void main(String[] args) {

        double v6 = method4(3, "40,85");

        System.out.println("Generated v6 value: " + v6);
    }

}
