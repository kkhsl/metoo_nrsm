package com.metoo.nrsm.core.system.conf.radvd.manager;

import com.metoo.nrsm.core.system.conf.radvd.service.AbstractRadvdService;
import com.metoo.nrsm.core.system.conf.radvd.service.LinuxRadvdService;
import com.metoo.nrsm.core.system.conf.radvd.service.WindowsRadvdService;
import com.metoo.nrsm.entity.Radvd;

import java.util.ArrayList;
import java.util.List;

/**
 * # 服务层配置管理
 */
public class RadvdConfigManager {

    public static void main(String[] args) {
        // 假设从数据库或前端获得了一个配置列表
        List<Radvd> radvdList = getRadvdListFromDatabase();

        // 根据平台选择对应的服务实现
        AbstractRadvdService service;
        if (isLinux()) {
            service = new LinuxRadvdService();
        } else {
            service = new WindowsRadvdService();
        }

        // 更新配置文件
        service.updateConfigFile(radvdList);
    }

    private static boolean isLinux() {
        // 检查系统平台
        return System.getProperty("os.name").toLowerCase().contains("linux");
    }

    private static List<Radvd> getRadvdListFromDatabase() {
        // 从数据库获取配置

        List<Radvd> list = new ArrayList<>();

        Radvd radvd = new Radvd();
        radvd.setName("test2");
        radvd.setInterfaceName("enp2s0f1.200");
        radvd.setIpv6Prefix("fc00:1000:0:1::/64");

        Radvd radvd2 = new Radvd();
        radvd2.setName("test1");
        radvd2.setInterfaceName("enp2s0f1.300");
        radvd2.setIpv6Prefix("fc00:1000:0:100::/64");

        list.add(radvd);
        list.add(radvd2);

        return list;
    }
}
