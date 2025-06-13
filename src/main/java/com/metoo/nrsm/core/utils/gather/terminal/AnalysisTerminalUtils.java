package com.metoo.nrsm.core.utils.gather.terminal;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.service.IPortIpv6Service;
import com.metoo.nrsm.core.service.IPortService;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.net.Ipv4Utils;
import com.metoo.nrsm.entity.Port;
import com.metoo.nrsm.entity.PortIpv6;
import com.metoo.nrsm.entity.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 1. 网段所有终端无IPv6地址：
 *    1. 找到配有该网段地址的交换机端口
 *    2. 该端口是否有全局IPv6地址和本地链路地址
 *    3. 如果有全局IPv6地址，无本地链路地址，则提示本地链路地址配置问题，给出对应品牌交换机的正确配置
 *    4. 如果无全局IPv6地址，则提示端口未配置IPv6，给出相关正确配置
 * 2. 网段部分终端无IPv6地址：
 *    1. 找到配有该网段地址的交换机端口
 *    2. 端口IPv6掩码不为64位，提示前缀掩码不为64位，安卓终端无法生成IPv6地址
 *    3. 端口IPv6掩码为64位，找是否有该终端的本地链路地址，如果没有，则是终端未开启ipv6，提示不同系统的开启方法
 *
 *
 */
@Slf4j
@Component
public class AnalysisTerminalUtils {

    // 注入springBean
    private final IPortService portService;
    private final IPortIpv6Service portIpv6Service;
    private final ITerminalService terminalService;

    public AnalysisTerminalUtils(IPortService portService, IPortIpv6Service portIpv6Service, ITerminalService terminalService) {
        this.portService = portService;
        this.portIpv6Service = portIpv6Service;
        this.terminalService = terminalService;
    }

    //根据终端ipv4地址，分析网段对应端口的ipv6情况
    public void analyze(){

        // 清空所有在线终端分析标记以及数据
        Map params = new HashMap();
        params.put("online", true);
        List<Terminal> onlineTerminalList = this.terminalService.selectObjByMap(params);
        if(!onlineTerminalList.isEmpty()){
            for (Terminal terminal : onlineTerminalList) {
                terminal.setConfig(0);
                terminal.setPortName(null);
                terminal.setPortSubne(null);
                terminal.setPortAddress(null);
                terminal.setPortIpv6Subnet(null);
                this.terminalService.update(terminal);
            }
        }
        // 第一步，获取status：2的ipv4 port数据
        params.clear();
        params.put("status", 1);
        params.put("ipIsNotNull", true);
        List<Port> portList = this.portService.selectObjByMap(params);
        if(portList.isEmpty()){
            return;
        }
        Map<String, String> portNetworkMap = new HashMap();
        // 第二步，遍历端口列表，获取ip地址对应主机位|网段
        Map<String, String> portHostMap = new HashMap();
        for (Port port : portList) {
            String ipv4 = port.getIp();
            String mask = port.getMask();
            if (ipv4 == null || mask == null) {
                continue;
            }
            String cidr = ipv4+"/"+mask;
            portHostMap.put(port.getPort(), cidr);
        }

        // 第三步，查询终端查询ipv4不为空（只取ipv4，不考虑多个ipv4地址情况）
        params.clear();
        params.put("v4IPIsNotNull", true);
        params.put("online", true);
        List<Terminal> terminalList = this.terminalService.selectObjByMap(params);
        if(terminalList.isEmpty()){
            return;
        }

        // 第四步，遍历终端，根据ip地址，判断属于哪个端口网段
        Map<String, List<Terminal>> portTerminalMap = new HashMap<>();
        for (Terminal terminal : terminalList) {
            String terminalIp = terminal.getV4ip();
            if (terminalIp == null) {
                continue;
            }
            // 检查终端IP是否属于某个端口网段(如果出现多个同网段设备，会导致接口选择错误)
            for (Map.Entry<String, String> entry : portHostMap.entrySet()) {
                String portName = entry.getKey(); // 例如 "eth0"
                String cidr = entry.getValue(); // 例如 "192.168.1.0/24"
                try {
                    String network = getNetwork(cidr);
                    portNetworkMap.put(portName, network);
                    if (isInNetwork(terminalIp, network)) {
                        // 如果属于该网段，添加到 portTerminalMap
                        portTerminalMap.computeIfAbsent(portName, k -> new ArrayList<>()).add(terminal);
                        break; // 找到一个匹配即可，避免重复（假设终端IP不会跨网段）
                    }
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
            }
        }

        List<String> portsWithV6Port = new ArrayList<>();// 网段部分终端无IPv6地址
        List<String> portsWithoutV6Port = new ArrayList<>();  // 网段所有终端无IPv6地址
        // 遍历端口map，查看端口下
        for (Map.Entry<String, List<Terminal>> entry : portTerminalMap.entrySet()) {
            String portName = entry.getKey();       // 例如 "eth0"
            List<Terminal> terminals = entry.getValue();

            boolean hasV6Terminal = false;

            // 检查该端口下的所有终端是否有IPv6地址
            for (Terminal terminal : terminals) {
                if (terminal.getV6ip() != null && !terminal.getV6ip().isEmpty()) {
                    hasV6Terminal = true;
                    break;  // 找到一个即停止
                }
            }
            // 根据结果分类
            if (hasV6Terminal) {
                portsWithV6Port.add(portName);
            } else {
                portsWithoutV6Port.add(portName);
            }
        }

        // 遍历端口列表
        for (String port : portsWithoutV6Port) {

            boolean isv6 = true;
            boolean isfe80 = false;

            String ipv6Subnet = "";

            // 查询v6端口列表
            params.clear();
            params.put("port", port);
            List<PortIpv6> portIpv6List = this.portIpv6Service.selectObjByMap(params);
            if(portIpv6List.isEmpty()){
                isv6 = false;
                isfe80 = true;
            }else{
                isv6 = false;
                for (PortIpv6 portIpv6 : portIpv6List) {
                    boolean hasFe80Ip = isFe80(portIpv6.getIpv6());
                    if(hasFe80Ip){
                        isfe80 = true;
                    }
                    if(isNoFe80(portIpv6.getIpv6())){
                        isv6 = true;
                        ipv6Subnet = portIpv6.getIpv6();
                    }
                }
            }

            // 获取端口名，获取网段
            String portNetwork = portNetworkMap.get(port);
            String portHost = portHostMap.get(port);
            for (Terminal terminal : portTerminalMap.get(port)) {
                if(!isv6){
                    terminal.setConfig(1);
                }else if(isv6 && !isfe80){
                    terminal.setPortIpv6Subnet(ipv6Subnet);
                    terminal.setConfig(4);
                }
                terminal.setPortSubne(portNetwork);
                terminal.setPortName(port);
                terminal.setPortAddress(portHost);
                this.terminalService.update(terminal);
            }
        }


        // 部分有v6地址
        for (String port : portsWithV6Port) {
            // 查询v6端口列表
            params.clear();
            params.put("port", port);
            List<PortIpv6> portIpv6List = this.portIpv6Service.selectObjByMap(params);
            if(portIpv6List.isEmpty()){
                continue;
            }

            // 是否存在FE80开头v6地址
            boolean withMask = false;
            boolean withoutMask = false;
            String ipv6Subnet = "";

            for (PortIpv6 portIpv6 : portIpv6List) {
                if(!portIpv6.isIpv6_local()){
                    ipv6Subnet = portIpv6.getIpv6();
                }
                if(portIpv6.getIpv6() != null && !portIpv6.getIpv6().isEmpty()){
                    if(portIpv6.getIpv6().contains("/")){
                        String ipv6 = portIpv6.getIpv6().split("/")[0];
                        String mask = portIpv6.getIpv6().split("/")[1];
                        if(Integer.parseInt(mask) == 64){
                            withMask = true;
                            continue;
                        }
                        withoutMask = true;
                    }
                }
            }

            String portNetwork = portNetworkMap.get(port);
            String portHost = portHostMap.get(port);
            for (Terminal terminal : portTerminalMap.get(port)) {
                if(withMask){
                    boolean hasFe80Ip = isFe80(terminal.getV6ip()) ||
                            isFe80(terminal.getV6ip1()) ||
                            isFe80(terminal.getV6ip2()) ||
                            isFe80(terminal.getV6ip3());
                    if(!hasFe80Ip){
                        terminal.setConfig(2);
                    }
                }else if(withoutMask){
                    terminal.setConfig(3);
                }
                terminal.setPortIpv6Subnet(ipv6Subnet);
                terminal.setPortName(port);
                terminal.setPortSubne(portNetwork);
                terminal.setPortAddress(portHost);
                this.terminalService.update(terminal);
            }
        }
        log.info("...");
    }


    /**
     * 根据IPv4地址和子网掩码计算网络地址（CIDR格式，如 "192.168.1.0/24"）
//     * @param ip   IPv4地址（如 "192.168.1.100"）
//     * @param mask   子网掩码（如 "255.255.255.0"）
     * @param cidr  （如 "192.168.1.100/255.255.255.0"）
     * @return 网络地址（如 "192.168.1.0/24"）
     */
    public static String getNetwork(String cidr) {
        try {
            String ip = cidr.split("/")[0];
            String mask = cidr.split("/")[1];
            // 将IP和掩码转换为字节数组
            byte[] ipBytes = InetAddress.getByName(ip).getAddress();
            byte[] maskBytes = InetAddress.getByName(mask).getAddress();

            // 计算网络地址（IP & 掩码）
            byte[] networkBytes = new byte[4];
            for (int i = 0; i < 4; i++) {
                networkBytes[i] = (byte) (ipBytes[i] & maskBytes[i]);
            }

            // 计算前缀长度（子网掩码中连续1的位数）
            int prefixLength = 0;
            for (byte b : maskBytes) {
                prefixLength += Integer.bitCount(b & 0xFF); // 统计每个字节的1的个数
            }

            // 组合结果：网络地址 + 前缀长度
            String networkAddress = InetAddress.getByAddress(networkBytes).getHostAddress();
            return networkAddress + "/" + prefixLength;

        } catch (UnknownHostException e) {
            throw new IllegalArgumentException("Invalid CIDR: " + cidr, e);
        }
    }

    public static boolean isInNetwork(String ip, String network) throws UnknownHostException {
        String[] parts = network.split("/");
        String networkAddress = parts[0];
        int prefixLength = Integer.parseInt(parts[1]);

        byte[] ipBytes = InetAddress.getByName(ip).getAddress();
        byte[] networkBytes = InetAddress.getByName(networkAddress).getAddress();

        // 比较前 prefixLength 位是否相同
        for (int i = 0; i < prefixLength / 8; i++) {
            if (ipBytes[i] != networkBytes[i]) {
                return false;
            }
        }
        return true;
    }

    public static void main(String[] args) {
        Terminal terminal = new Terminal();


        // 只要有一个IP是fe80开头，就返回1
//        boolean hasFe80Ip = isFe80(terminal.getV6ip()) ||
//                isFe80(terminal.getV6ip1()) ||
//                isFe80(terminal.getV6ip2()) ||
//                isFe80(terminal.getV6ip3());
//        // 逻辑判断
//        if (hasFe80Ip) {
//            System.out.println(1); // 有fe80开头IP → 不符合
//        } else {
//            System.out.println(2); // 全部IP都不是fe80 → 符合
//        }
//        System.out.println(isFe80(""));
//

        boolean hasV6 = isNoFe80(terminal.getV6ip()) ||
                isNoFe80(terminal.getV6ip1()) ||
                isNoFe80(terminal.getV6ip2()) ||
                isNoFe80(terminal.getV6ip3());

        if (hasV6) {
            System.out.println(4);
        } else {
            System.out.println(1);
        }
    }

    /**
     * 判断IP是否是fe80开头（非null且非空）
     */
    private static boolean isFe80(String ip) {
        return ip != null &&
                !ip.isEmpty() &&  // 替换了StringUtil.isEmpty()
                ip.toLowerCase().startsWith("fe80");
    }

    private static boolean isNoFe80(String ip) {
        return ip != null &&
                !ip.isEmpty() &&
                !ip.toLowerCase().startsWith("fe80");
    }
}
