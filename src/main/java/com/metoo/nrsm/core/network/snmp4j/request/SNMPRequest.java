package com.metoo.nrsm.core.network.snmp4j.request;

import com.metoo.nrsm.core.network.snmp4j.constants.SNMP_OID;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.response.SNMPDataParser;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.net.NoRouteToHostException;
import java.net.UnknownHostException;

/**
 * 设计线程安全方案
 */
public class SNMPRequest {

//    private static TransportMapping transport;
//    private static Snmp snmp;

    // 初始化 TransportMapping 和 Snmp 实例
//    static {
//        try {
//            transport = new DefaultUdpTransportMapping();
//            snmp = new Snmp(transport);
//            transport.listen(); // 启动监听
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    private static ThreadLocal<Snmp> threadSnmp = ThreadLocal.withInitial(() -> {
        try {
            TransportMapping transport = new DefaultUdpTransportMapping();
            Snmp snmp = new Snmp(transport);
            transport.listen();
            return snmp;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    });

    // 发送 SNMP 请求并返回原始响应数据
    public static PDU sendRequest(SNMPParams snmpParams, SNMP_OID snmpOid) {
        Snmp snmp = threadSnmp.get();  // 获取当前线程的 Snmp 实例
        if (snmp == null) {
            return null;  // 如果 Snmp 实例为空，返回 null
        }

        try {
            // 设置 SNMP 目标地址
            String targetAddress = "udp:" + snmpParams.getIp() + "/161";
            Address target = GenericAddress.parse(targetAddress);

            // 配置社区字符串和版本
            CommunityTarget communityTarget = new CommunityTarget();
            communityTarget.setCommunity(new OctetString(snmpParams.getCommunity()));
            if ("v2c".equalsIgnoreCase(snmpParams.getVersion())) {
                communityTarget.setVersion(SnmpConstants.version2c);  // SNMP v2c
            } else if ("v3".equalsIgnoreCase(snmpParams.getVersion())) {
                communityTarget.setVersion(SnmpConstants.version3);  // SNMP v3
            } else {
                communityTarget.setVersion(SnmpConstants.version1);  // SNMP v1 默认
            }
            communityTarget.setAddress(target);
            communityTarget.setRetries(3);// 重试
            communityTarget.setTimeout(3000);

            // 创建 PDU 请求
            PDU pdu = new PDU();
            pdu.add(new VariableBinding(new OID(snmpOid.getOid())));
            pdu.setType(PDU.GET);

            // 发送请求并接收响应
            ResponseEvent response = snmp.send(pdu, communityTarget);
            if (response == null) {
                return null;  // 没有收到响应
            } else {
                return response.getResponse();  // 返回响应 PDU
            }
        } catch (NoRouteToHostException e) {
            System.err.println("Network error: No route to host. " + e.getMessage());
            e.printStackTrace();
        } catch (UnknownHostException e) {
            System.err.println("Unknown host: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("IO Exception while sending SNMP request: " + e.getMessage());
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            // 关闭 snmp 实例并释放 TransportMapping 资源
            try {
                if (snmp != null) {
                    snmp.close();   // 关闭 snmp 实例（会自动关闭 TransportMapping）
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }


    // 发送 SNMP 请求并返回解析后的设备名
    public static String getDeviceName(SNMPParams snmpParams) {
        PDU responsePdu = sendRequest(snmpParams, SNMP_OID.HOST_NAME);
        return SNMPDataParser.parseDeviceName(responsePdu);
    }





}
