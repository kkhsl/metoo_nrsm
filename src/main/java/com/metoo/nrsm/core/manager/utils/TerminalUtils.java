package com.metoo.nrsm.core.manager.utils;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.wsapi.utils.RedisResponseUtils;
import com.metoo.nrsm.entity.DeviceType;
import com.metoo.nrsm.entity.Terminal;
import com.metoo.nrsm.entity.TerminalMacIpv6;
import com.metoo.nrsm.entity.Vendor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TerminalUtils {


    @Autowired
    private IDeviceTypeService deviceTypeService;
    @Autowired
    private IVendorService vendorService;
    @Autowired
    private ITerminalMacIpv6Service terminalMacIpv6Service;


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


    public void completeTerminal(Terminal terminal) {

        DeviceType deviceType = deviceTypeService.selectObjById(terminal.getDeviceTypeId());
        if (deviceType != null) {
            terminal.setDeviceTypeName(deviceType.getName());
            terminal.setDeviceTypeUuid(deviceType.getUuid());
        }
        if (terminal.getVendorId() != null && !terminal.getVendorId().equals("")) {
            Vendor vendor = this.vendorService.selectObjById(terminal.getVendorId());
            if (vendor != null) {
                terminal.setVendorName(vendor.getName());
            }
        }
        if (StringUtil.isNotEmpty(terminal.getMac())) {
            TerminalMacIpv6 terminalMacIpv6 = this.terminalMacIpv6Service.getMacByMacAddress(terminal.getMac());
            if (terminalMacIpv6 != null && terminalMacIpv6.getIsIPv6() == 1) {
                terminal.setIsIpv6(1);
            } else {
                terminal.setIsIpv6(0);
            }
        }

        if (terminal.getV6ip() != null && terminal.getV6ip().toLowerCase().startsWith("fe80")) {
            terminal.setV6ip(null);
        }
        if (terminal.getV6ip1() != null && terminal.getV6ip1().toLowerCase().startsWith("fe80")) {
            terminal.setV6ip1(null);
        }
        if (terminal.getV6ip2() != null && terminal.getV6ip2().toLowerCase().startsWith("fe80")) {
            terminal.setV6ip2(null);
        }
        if (terminal.getV6ip3() != null && terminal.getV6ip3().toLowerCase().startsWith("fe80")) {
            terminal.setV6ip3(null);
        }
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
