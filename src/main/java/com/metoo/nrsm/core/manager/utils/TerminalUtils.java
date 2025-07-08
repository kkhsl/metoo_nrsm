package com.metoo.nrsm.core.manager.utils;

import com.metoo.nrsm.entity.Terminal;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TerminalUtils {

    public static List<Terminal> different(List<Terminal> list1, List<Terminal> list2) {
        List<Terminal> differentTerminals1 = list1.stream()
                .filter(t1 -> list2.stream().noneMatch(t2 -> t2.getMac().equals(t1.getMac())))
                .collect(Collectors.toList());
        differentTerminals1.forEach(System.out::println);
        return differentTerminals1;
    }

    public static List<Terminal> common(List<Terminal> list1, List<Terminal> list2) {
        List<Terminal> commonTerminals = list1.stream()
                .filter(t1 -> list2.stream().anyMatch(t2 -> t1.getMac().equals(t2.getMac())))
                .collect(Collectors.toList());

        return commonTerminals;
    }


    public static void main(String[] args) {
        List<TerminalTest> list1 = new ArrayList<>();
        List<TerminalTest> list2 = new ArrayList<>();

        // 示例数据
        list1.add(new TerminalTest("00:11:22:33:44:55"));
        list1.add(new TerminalTest("11:22:33:44:55:66"));
        list1.add(new TerminalTest("11:22:33:44:55:636"));
        list1.add(new TerminalTest("11:22:33:44:55:646"));

        list2.add(new TerminalTest("00:11:22:33:44:55"));
        list2.add(new TerminalTest("22:33:44:55:66:77"));


        List<TerminalTest> differentTerminals = list1.stream()
                .filter(t1 -> list2.stream().noneMatch(t2 -> t2.getMac().equals(t1.getMac())))
                .collect(Collectors.toList());
        System.out.println("不同的 Terminal:");
        differentTerminals.forEach(System.out::println);


        List<TerminalTest> differentTerminals1 = list1.stream()
                .filter(t1 -> list2.stream().noneMatch(t2 -> t2.getMac().equals(t1.getMac())))
                .collect(Collectors.toList());

        System.out.println("不同的 Terminal:");
        differentTerminals1.forEach(System.out::println);

        // 找出 mac 不同的 terminal
        List<TerminalTest> differentTerminals2 = list2.stream()
                .filter(t2 -> list1.stream().noneMatch(t1 -> t1.getMac().equals(t2.getMac())))
                .collect(Collectors.toList());


        // 输出不同的 terminal
        System.out.println("不同的 Terminal:");
        differentTerminals2.forEach(System.out::println);

        // 找出 mac 相同的 terminal
        List<TerminalTest> commonTerminals = list1.stream()
                .filter(t1 -> list2.stream().anyMatch(t2 -> t1.getMac().equals(t2.getMac())))
                .collect(Collectors.toList());

        // 输出相同的 terminal
        System.out.println("相同的 Terminal:");
        commonTerminals.forEach(System.out::println);

    }

}


class TerminalTest {
    private String mac;

    public TerminalTest(String mac) {
        this.mac = mac;
    }

    public String getMac() {
        return mac;
    }

    // toString() 方法用于输出
    @Override
    public String toString() {
        return "Terminal{mac='" + mac + "'}";
    }
}
