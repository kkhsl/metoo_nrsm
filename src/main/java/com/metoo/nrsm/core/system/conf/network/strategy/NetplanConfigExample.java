package com.metoo.nrsm.core.system.conf.network.strategy;

import com.metoo.nrsm.entity.Interface;

public class NetplanConfigExample {

    public static void main(String[] args) {
        try {
            // 示例1: 配置普通以太网接口
//            Interface eth0 = new Interface();
//            eth0.setName("eno1");
//            eth0.setIpv4Address("192.168.5.102/24");
//            eth0.setIpv6Address("fc00:1000:0:50::3/64");
//            eth0.setGateway4("192.168.5.1");
//
//            NetplanConfigManager.updateInterfaceConfig(eth0);
//            System.out.println("enp2s0f0配置更新成功");

            // 示例2: 配置VLAN接口
            Interface vlan200 = new Interface();
            vlan200.setVlanNum(200);
            vlan200.setIpv4Address("192.168.6.102/24");
            vlan200.setIpv6Address("fc00:1000:0:1::3/64");
            vlan200.setGateway4("192.168.6.1");
            vlan200.setParentName("eno1");

            NetplanConfigManager.updateInterfaceConfig(vlan200);
            System.out.println("VLAN 200配置更新成功");

            // 如果需要恢复配置
            // NetplanConfigManager.restoreConfig();

            // 示例3：删除vlan接口
//            NetplanConfigManager.removeVlanInterface("eno1.200");

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
