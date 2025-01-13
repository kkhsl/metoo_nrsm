package com.metoo.nrsm.core.config.utils;

import java.util.Random;

/**

 Description: 生成随机盐

 */

public class SaltUtils {

    // 生成指定长度随即字符串
    public static String getSalt(int length){
       char[] numbersAndLetters = ("0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ9876543210abcdefghijklmnopqrstuvwxyz").toCharArray();
       if(length < 1){
        return "";
       }
        Random random = new Random();
        char[] randGen = new char[length];
        for(int i = 0; i< randGen.length; i++){
            randGen[i] = numbersAndLetters[random.nextInt(numbersAndLetters.length)];
        }
        return new String(randGen);
    }

    public static void main(String[] args) {


        System.out.println(getSalt(4));
    }
}


