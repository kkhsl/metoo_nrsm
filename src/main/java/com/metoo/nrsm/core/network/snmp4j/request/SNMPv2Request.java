package com.metoo.nrsm.core.network.snmp4j.request;

import com.metoo.nrsm.core.network.networkconfig.DHCPUtil;
import com.metoo.nrsm.core.network.snmp4j.constants.SNMP_OID;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.response.SNMPDataParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snmp4j.CommunityTarget;
import org.snmp4j.PDU;
import org.snmp4j.Snmp;
import org.snmp4j.TransportMapping;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.metoo.nrsm.core.network.snmp4j.response.SNMPDataParser.convertOidToMac;

/**
 * 设计线程安全方案
 */
public class SNMPv2Request {

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

    // Helper method to configure SNMP target (community, address, timeout, etc.)
    private static CommunityTarget configureTarget(SNMPParams snmpParams) {
        String targetAddress = "udp:" + snmpParams.getIp() + "/161";
        Address target = GenericAddress.parse(targetAddress);
        CommunityTarget communityTarget = new CommunityTarget();
        communityTarget.setCommunity(new OctetString(snmpParams.getCommunity()));
        communityTarget.setVersion(SnmpConstants.version2c);
        communityTarget.setAddress(target);
        communityTarget.setTimeout(5000);
        communityTarget.setRetries(5);
        return communityTarget;
    }

    // Sending SNMP request
    public static PDU sendRequest(SNMPParams snmpParams, SNMP_OID snmpOid) {
        Snmp snmp = threadSnmp.get();
        if (snmp == null) {
            System.err.println("SNMP 实例初始化失败");
            return null;
        }

        try {
            CommunityTarget communityTarget = configureTarget(snmpParams);
            PDU pdu = new PDU();
            OID oid = new OID(snmpOid.getOid());
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);

            ResponseEvent response = snmp.send(pdu, communityTarget);
            PDU responsePDU = response.getResponse();

            if (responsePDU == null) {
                System.err.println("无响应(超时或目标不可达)");
            }
            if (responsePDU != null && responsePDU.getErrorStatus() != PDU.noError) {
                System.err.println("无响应(超时或目标不可达) 或者SNMP 错误" + responsePDU.getErrorStatusText());
            }
            return responsePDU;

        } catch (Exception e) {
            System.err.println("请求异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    public static PDU sendStrRequest(SNMPParams snmpParams, String snmpOid) {
        Snmp snmp = threadSnmp.get();
        if (snmp == null) {
            System.err.println("SNMP 实例初始化失败");
            return null;
        }

        try {
            CommunityTarget communityTarget = configureTarget(snmpParams);
            PDU pdu = new PDU();
            OID oid = new OID(snmpOid);
            pdu.add(new VariableBinding(oid));
            pdu.setType(PDU.GET);

            ResponseEvent response = snmp.send(pdu, communityTarget);
            PDU responsePDU = response.getResponse();

            if (responsePDU == null) {
                System.err.println("无响应(超时或目标不可达) ");
            }
            if(responsePDU != null && responsePDU.getErrorStatus() != PDU.noError){
                System.err.println("无响应(超时或目标不可达) 或者SNMP 错误" + responsePDU.getErrorStatusText());
            }
            return responsePDU;

        } catch (Exception e) {
            System.err.println("请求异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }

    private static Map<String, String> sendGETNEXTRequest(SNMPParams snmpParams, SNMP_OID snmpOid) {
        // 用于存储最终的 ARP 表项
        Map<String, String> arpResultMap = new HashMap<>();

        // 获取 SNMP 实例
        Snmp snmp = threadSnmp.get();
        if (snmp == null) {
            System.err.println("SNMP 实例初始化失败");
            return null;
        }

        try {
            // 配置 SNMP 目标
            CommunityTarget target = configureTarget(snmpParams);
            OID oid = new OID(snmpOid.getOid());

            while (true) {
                // 创建 PDU 请求，设置为 GETNEXT
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(oid));
                pdu.setType(PDU.GETNEXT);

                // 发送 SNMP 请求
                ResponseEvent response = snmp.send(pdu, target);
                PDU responsePDU = response.getResponse();

                // 如果没有响应或者发生错误，退出
                if (responsePDU == null || responsePDU.getErrorStatus() != 0) {
                    System.err.println("SNMP 请求失败或无响应！");
                    break;
                }

                // 获取响应中的 VariableBinding
                VariableBinding vb = responsePDU.get(0);


                // 如果 OID 不再属于目标范围，退出
                if (!vb.getOid().toString().startsWith(snmpOid.getOid())) {
                    break;
                }

                // 以 OID 为 key，将其对应的值加入到 Map 中
                String key = vb.getOid().toString();
                String value = vb.getVariable().toString();
                arpResultMap.put(key, value);

                // 更新 OID，准备下一次请求
                oid = vb.getOid();


            }
        } catch (Exception e) {
            System.err.println("请求异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return arpResultMap;
    }
    private static Map<String, String> sendStrGETNEXTRequest(SNMPParams snmpParams, String snmpOid) {
        // 用于存储最终的 ARP 表项
        Map<String, String> arpResultMap = new HashMap<>();

        // 获取 SNMP 实例
        Snmp snmp = threadSnmp.get();
        if (snmp == null) {
            System.err.println("SNMP 实例初始化失败");
            return null;
        }

        try {
            // 配置 SNMP 目标
            CommunityTarget target = configureTarget(snmpParams);
            OID oid = new OID(snmpOid);

            while (true) {
                // 创建 PDU 请求，设置为 GETNEXT
                PDU pdu = new PDU();
                pdu.add(new VariableBinding(oid));
                pdu.setType(PDU.GETNEXT);

                // 发送 SNMP 请求
                ResponseEvent response = snmp.send(pdu, target);
                PDU responsePDU = response.getResponse();

                // 如果没有响应或者发生错误，退出
                if (responsePDU == null || responsePDU.getErrorStatus() != 0) {
                    System.err.println("SNMP 请求失败或无响应！");
                    break;
                }

                // 获取响应中的 VariableBinding
                VariableBinding vb = responsePDU.get(0);


                // 如果 OID 不再属于目标范围，退出
                if (!vb.getOid().toString().startsWith(snmpOid)) {
                    break;
                }

                // 以 OID 为 key，将其对应的值加入到 Map 中
                String key = vb.getOid().toString();
                String value = vb.getVariable().toString();
                arpResultMap.put(key, value);

                // 更新 OID，准备下一次请求
                oid = vb.getOid();


            }
        } catch (Exception e) {
            System.err.println("请求异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
            e.printStackTrace();
            return null;
        }
        return arpResultMap;
    }

    //解析 SNMP 响应，获取设备名
    public static String getDeviceName(SNMPParams snmpParams) {
        PDU responsePdu = sendRequest(snmpParams, SNMP_OID.HOST_NAME);
        return SNMPDataParser.parseDeviceName(responsePdu);
    }

    //解析 SNMP 响应，获取arp
    public static String getDeviceArp(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 ARP 表遍历
        Map<String, String> arpMap = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceArp(arpMap));
    }


    public static String getDeviceArpPort(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 ArpPort 表遍历
        Map<String, String> arpPortMap = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP_PORT);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceArpPort(arpPortMap));
    }

    public static String getDeviceArpV6(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 ArpV6 表遍历
        Map<String, String> arpV6Map = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP_V6);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceArpV6(arpV6Map));
    }

    public static String getDeviceArpV6Port(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 ArpV6Port 表遍历
        Map<String, String> arpV6PortMap = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP_V6_PORT);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceArpPort(arpV6PortMap));
    }

    public static String getDevicePort(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 Port 表遍历
        Map<String, String> portMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT);
        /*Map<String, String> portMap = new HashMap<>();
        // 赋值
        portMap.put("52","GigabitEthernet1/0/52");
        portMap.put("12","GigabitEthernet1/0/12");*/
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePort(portMap));
    }

    public static String getDevicePortIp(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortIp 表遍历
        Map<String, String> portIpMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_IP);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePortIp(portIpMap));
    }

    public static String getDevicePortV6(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortV6 表遍历
        Map<String, String> portV6Map = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_IPV6);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePortV6(portV6Map));
    }

    public static String getDevicePortMask(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortMask 表遍历
        Map<String, String> portMaskMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_MASK);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePortMask(portMaskMap));
    }

    public static String getDevicePortDescription(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortDescription 表遍历
        Map<String, String> portDescriptionMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_DESCRIPTION);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePortDescription(portDescriptionMap));
    }

    public static String getDeviceMac(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 Mac 表遍历
        Map<String, String> macMap = sendGETNEXTRequest(snmpParams, SNMP_OID.MAC);
        /* Map<String, String> macMap = new HashMap<>();
        // 赋值
        macMap.put("1.3.6.1.2.1.17.4.3.1.2.0.11.95.228.214.24","52");  //1.3.6.1.2.1.17.4.3.1.2.0.11.95.228.214.0   163
        macMap.put("1.3.6.1.2.1.17.4.3.1.2.48.67.215.235.184.16","12");
        */
        String str = SNMP_OID.MAC.getOid() + ".";
        String strNew;
        String indexNew = null;

        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : macMap.entrySet()) {
            String oid = entry.getKey(); // OID
            String index = entry.getValue(); // 端口值
            strNew = "1.3.6.1.2.1.17.1.4.1.2." + index;
            PDU pdu = sendStrRequest(snmpParams, strNew);
            indexNew = pdu.getVariableBindings().firstElement().toString().split("=")[1].trim().replace("= ", "").replace("\"", "");
            String newOid = oid.replace(str, "");
            // 提取 MAC 地址部分
            String macAddress = convertOidToMac(newOid);
            result.put(macAddress, indexNew);
        }

        return SNMPDataParser.convertToJson(result);
    }

    public static String getDeviceMac2(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 Mac 表遍历
        Map<String, String> mac2Map = sendGETNEXTRequest(snmpParams, SNMP_OID.MAC2);
        String str = SNMP_OID.MAC2.getOid() + ".";
        String strNew;
        String indexNew = null;

        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : mac2Map.entrySet()) {
            String oid = entry.getKey(); // OID
            String index = entry.getValue(); // 端口值
            strNew = "1.3.6.1.2.1.17.1.4.1.2." + index;
            PDU pdu = sendStrRequest(snmpParams, strNew);
            indexNew = pdu.getVariableBindings().firstElement().toString().split("=")[1].trim().replace("= ", "").replace("\"", "");
            String newOid = oid.replace(str, "");
            // 提取 MAC 地址部分
            String macAddress = convertOidToMac(newOid);
            result.put(macAddress, indexNew);
        }

        return SNMPDataParser.convertToJson(result);
    }
/*    public static String getDeviceMac3(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 Mac 表遍历
        Map<String, String> mac3Map = sendGETNEXTRequest(snmpParams, SNMP_OID.MAC3);
        String str = SNMP_OID.MAC3.getOid() + ".";
        String strNew;
        String indexNew = null;

        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : mac3Map.entrySet()) {
            String oid = entry.getKey(); // OID
            String index = entry.getValue(); // 端口值
            strNew = "1.3.6.1.2.1.17.1.4.1.2." + index;
            PDU pdu = sendStrRequest(snmpParams, strNew);
            indexNew = pdu.getVariableBindings().firstElement().toString().split("=")[1].trim().replace("= ", "").replace("\"", "");
            String newOid = oid.replaceFirst(
                    Pattern.quote(str) + "\\d+\\.", // 匹配 str 后紧跟的"数字+."
                    ""
            );
            // 提取 MAC 地址部分
            String macAddress = convertOidToMac(newOid);
            result.put(macAddress, indexNew);
        }

        return SNMPDataParser.convertToJson(result);
    }*/


    /*public static String getDeviceMac3(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 Mac 表遍历
        Map<String, String> mac3Map = sendGETNEXTRequest(snmpParams, SNMP_OID.MAC3);
        String str = SNMP_OID.MAC3.getOid() + ".";
        String strNew;
        String indexNew = null;

        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : mac3Map.entrySet()) {
            String oid = entry.getKey(); // OID
            String index = entry.getValue(); // 端口值
            *//*strNew = "1.3.6.1.2.1.17.1.4.1.2." + index;
            PDU pdu = sendStrRequest(snmpParams, strNew);
            indexNew = pdu.getVariableBindings().firstElement().toString().split("=")[1].trim().replace("= ", "").replace("\"", "");

            );*//*
            // 提取 MAC 地址部分

            String newOid = oid.replaceFirst(
                    Pattern.quote(str) + "\\d+\\.", // 匹配 str 后紧跟的"数字+."
                    "");
            String macAddress = convertOidToMac(newOid);
            result.put(macAddress, index);
        }

        return SNMPDataParser.convertToJson(result);
    }*/


    public static String getDeviceMac3(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 Mac 表遍历
        Map<String, String> mac3Map = sendGETNEXTRequest(snmpParams, SNMP_OID.MAC3);
        String str = SNMP_OID.MAC3.getOid() + ".";
        String strNew;
        String indexNew = null;

        Map<String, String> result = new HashMap<>();

        for (Map.Entry<String, String> entry : mac3Map.entrySet()) {
            String oid = entry.getKey(); // OID
            String index = entry.getValue(); // 端口值

            // 优化后的OID处理逻辑：从OID末尾提取6组数字
            String newOid;

            // 从OID字符串末尾反向查找最近6个连续的数字组
            int lastDotPosition = oid.lastIndexOf('.'); // 找到最后一个点
            int count = 0;
            int startIndex = -1;

            // 反向遍历找到第6个数字组的起始位置
            for (int i = oid.length() - 1; i >= 0; i--) {
                char c = oid.charAt(i);
                if (c == '.') {
                    count++;
                    if (count == 6) {
                        startIndex = i + 1; // 获取6个数字组的起始位置
                        break;
                    }
                }
            }

            if (startIndex != -1) {
                // 成功找到6个连续的组，取最后6个部分
                newOid = oid.substring(startIndex);

                // 如果有额外的部分（如最后的90），只保留6组数字
                String[] parts = newOid.split("\\.");
                if (parts.length > 6) {
                    newOid = String.join(".", Arrays.copyOfRange(parts, 0, 6));
                }
            } else {
                // 回退逻辑：如果无法提取6个组，使用原算法
                newOid = oid.replaceFirst(
                        Pattern.quote(str) + "\\d+\\.",
                        "");
            }

            String macAddress = convertOidToMac(newOid);
            result.put(macAddress, index);
        }

        return SNMPDataParser.convertToJson(result);
    }

    public static String getDevicePortStatus(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortStatus 表遍历
        Map<String, String> portStatusMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_STATUS);
        return SNMPDataParser.convertToJson(SNMPDataParser.parsePortStatus(portStatusMap));
    }

    public static String getDevicePortMac(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortMac 表遍历
        Map<String, String> portMacMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_MAC);
        return SNMPDataParser.convertToJson(SNMPDataParser.parsePortMac(portMacMap));
    }

    public static String getDeviceMacType(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 MacType 表遍历
        Map<String, String> macTypeMap = sendGETNEXTRequest(snmpParams, SNMP_OID.MAC_TYPE);
         /*Map<String, String> macTypeMap = new HashMap<>();
        // 赋值
        macTypeMap.put("1.3.6.1.2.1.17.4.3.1.3.0.11.95.228.214.24", "3");   //1.3.6.1.2.1.17.4.3.1.3.0.11.95.228.214.0   3
        macTypeMap.put("1.3.6.1.2.1.17.4.3.1.3.48.67.215.235.184.16", "3");*/
        String str = SNMP_OID.MAC_TYPE.getOid() + ".";
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceMacType(macTypeMap, str));
    }

    public static String getDeviceUpdateTime(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 MacType 表遍历
        PDU updateTimeMap = sendRequest(snmpParams, SNMP_OID.UP_TIME);
        return SNMPDataParser.parseDeviceUpdateTime(updateTimeMap);
    }

    public static String getV6Device(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 V6Device 表遍历
        Map<String, String> v6Device = sendGETNEXTRequest(snmpParams, SNMP_OID.IPV6_DEVICE);
        return SNMPDataParser.convertToJson(SNMPDataParser.parsePortMac(v6Device));
    }

    public static String getLLDP(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 LLDP 表遍历
        Map<String, String> lldp = sendGETNEXTRequest(snmpParams, SNMP_OID.LLDP);
        /*Map<String, String> lldp = new HashMap<>();
        // 赋值
        lldp.put("1.0.8802.1.1.2.1.4.1.1.9.8359.52.1", "zhihuigu_1L_HJ_SW");*/
        return SNMPDataParser.convertToJson(SNMPDataParser.parseLLDP(lldp));
    }

    public static String getLLDPPort(SNMPParams snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 LLDP 表遍历
        Map<String, String> lldpPort = sendGETNEXTRequest(snmpParams, SNMP_OID.LLDP_ROMOTE_PORT);
        /*Map<String, String> lldpPort = new HashMap<>();
        // 赋值
        lldpPort.put("1.0.8802.1.1.2.1.4.1.1.7.8359.52.1", "GigabitEthernet1/0/1");
        lldpPort.put("1.0.8802.1.1.2.1.4.1.1.7.256479241.35.1", "28 D2 44 0F 08 DA");  //Hex-28D2440F08DA
        lldpPort.put("1.0.8802.1.1.2.1.4.1.1.7.256586179.43.1", "B8 88 E3 EB 40 AE");  //Hex-B888E3EB40AE*/
        return SNMPDataParser.convertToJson(SNMPDataParser.parseLLDPPort(lldpPort));
    }


    public static Boolean getIsV6(SNMPParams snmpParams) {
        // 调用 sendRequest 方法进行 SNMP 请求
        PDU isV6 = sendRequest(snmpParams, SNMP_OID.IS_IPV6);
        return SNMPDataParser.parseIsV6(isV6);
    }

    public static String getTraffic(SNMPParams snmpParams,String oid1,String oid2) {
        // 调用 sendArpRequest 方法进行 SNMP 请求
        PDU in = sendStrRequest(snmpParams, oid1);
        PDU out = sendStrRequest(snmpParams,oid2);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseTraffic(in,out));
    }


    // 获取 ARP 表、端口和端口映射
    public static JSONArray getArp(SNMPParams snmpParams) {
        String arpData = SNMPv2Request.getDeviceArp(snmpParams);
        String arpPortData = SNMPv2Request.getDeviceArpPort(snmpParams);
        String portData = SNMPv2Request.getDevicePort(snmpParams);
        // 解析 JSON 数据
        JSONObject arpJson = new JSONObject(arpData);
        JSONObject arpPortJson = new JSONObject(arpPortData);
        JSONObject portJson = new JSONObject(portData);

        // 创建结果数组
        JSONArray resultArray = new JSONArray();

        // 遍历 ARP 数据
        for (String ip : arpJson.keySet()) {
            String mac = arpJson.getString(ip);
            String portNumber = arpPortJson.optString(ip, null);
            String portName = portJson.optString(portNumber, null);

            // 创建结果对象
            JSONObject resultObject = new JSONObject();
            resultObject.put("mac", mac);
            resultObject.put("ip", ip);
            resultObject.put("port", portName != null ? portName : "unknown");

            // 添加到结果数组
            resultArray.put(resultObject);
        }
        return resultArray;
    }

    //获取 ARPV6 表、端口和端口映射
    public static JSONArray getArpV6(SNMPParams snmpParams) {
        Map<String, String> arpV6Map = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP_V6);
        String portData = SNMPv2Request.getDevicePort(snmpParams);
        JSONObject portJson = new JSONObject(portData);
        JSONArray resultArray = SNMPDataParser.parseDeviceArpV6(arpV6Map, portJson);
        return resultArray;
    }



    public static JSONArray getPortMac(SNMPParams snmpParams) {
        // 获取 SNMP 数据
        String macData = SNMPv2Request.getDevicePortMac(snmpParams);
        String statusData = SNMPv2Request.getDevicePortStatus(snmpParams);
        String portData = SNMPv2Request.getDevicePort(snmpParams);


        // 解析 JSON 数据
        JSONObject macJson = new JSONObject(macData);
        JSONObject statusJson = new JSONObject(statusData);
        JSONObject portJson = new JSONObject(portData);

        // 创建结果数组
        JSONArray resultArray = new JSONArray();

        // 遍历 MAC 数据
        for (String portNumber : macJson.keySet()) {
            String mac = macJson.getString(portNumber);
            String status = statusJson.optString(portNumber, null);
            String portName = portJson.optString(portNumber, null);

            // 过滤条件：跳过 MAC 为空或端口名称为 'null0'
            if (!mac.isEmpty() && !portName.equals("null0")) {
                JSONObject resultObject = new JSONObject();
                resultObject.put("mac", mac);
                resultObject.put("port", portName != null ? portName : "unknown");
                resultObject.put("status", status != null ? status : "unknown");
                // 添加到结果数组
                resultArray.put(resultObject);
            }
        }
        return resultArray;
    }

    public static JSONArray getPortTable(SNMPParams snmpParams) {
        // 获取 SNMP 数据
        String portData = SNMPv2Request.getDevicePort(snmpParams);
        String statusData = SNMPv2Request.getDevicePortStatus(snmpParams);
        String portIpData = SNMPv2Request.getDevicePortIp(snmpParams);
        String portMaskData = SNMPv2Request.getDevicePortMask(snmpParams);
        String portDescriptionData = SNMPv2Request.getDevicePortDescription(snmpParams);

        // 解析 JSON 数据
        JSONObject portJson = new JSONObject(portData);
        JSONObject statusJson = new JSONObject(statusData);
        JSONObject portIpJson = new JSONObject(portIpData);
        JSONObject portMaskJson = new JSONObject(portMaskData);
        JSONObject portDescriptionJson = new JSONObject(portDescriptionData);

        // 创建结果数组
        JSONArray resultArray = new JSONArray();

        // 遍历端口数据
        for (String portNumber : portJson.keySet()) {
            String portName = portJson.getString(portNumber);
            String status = statusJson.optString(portNumber, "unknown");
            String ip = ipFromPort(portNumber, portIpJson); // 直接获取 IP
            String mask = portMaskJson.optString(ip, ""); // 根据 IP 获取掩码
            String description = portDescriptionJson.optString(portNumber, "");

            // 创建结果对象
            JSONObject resultObject = new JSONObject();
            resultObject.put("port", portName != null ? portName : "unknown");
            resultObject.put("status", status);
            resultObject.put("ip", ip != null ? ip : "");
            resultObject.put("mask", mask);
            resultObject.put("description", description);

            // 添加到结果数组
            resultArray.put(resultObject);
        }

        return resultArray;
    }

    public static JSONArray getPortTableV6(SNMPParams snmpParams) {
        Map<String, String> portV6Map = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_IPV6);
        // 获取 SNMP 数据
        String portData = SNMPv2Request.getDevicePort(snmpParams);
        // 解析 JSON 数据
        JSONObject portJson = new JSONObject(portData);

        JSONArray resultArray = SNMPDataParser.parseDevicePortV6(portV6Map, portJson);

        return resultArray;
    }

    // 辅助方法：根据端口号获取 IP 地址
    private static String ipFromPort(String portNumber, JSONObject portIpJson) {
        for (String ip : portIpJson.keySet()) {
            if (portIpJson.getString(ip).equals(portNumber)) {
                return ip;
            }
        }
        return ""; // 确保返回空字符串而不是 null
    }


    /*public static JSONArray getMac(SNMPParams snmpParams) {
        // 获取 SNMP 数据
        String macData = SNMPv2Request.getDeviceMac3(snmpParams);
        String macTypeData = SNMPv2Request.getDeviceMacType(snmpParams);
        String portData = SNMPv2Request.getDevicePort(snmpParams);

        // 解析 JSON 数据
        JSONObject macJson = new JSONObject(macData);
        JSONObject macTypeJson = new JSONObject(macTypeData);
        JSONObject portJson = new JSONObject(portData);

        // 创建结果数组
        JSONArray resultArray = new JSONArray();

        // 遍历 MAC 数据
        for (String mac : macJson.keySet()) {
            String portNumber = macJson.getString(mac); // 获取端口号
            String portName = portJson.optString(portNumber, "unknown"); // 根据端口号获取端口名称
            String macType = macTypeJson.optString(mac, "unknown"); // 获取 MAC 类型

            // 创建结果对象
            JSONObject resultObject = new JSONObject();
            resultObject.put("mac", mac);
            resultObject.put("port", portName); // 端口名称
            resultObject.put("type", macType); // MAC 类型

            // 添加到结果数组
            resultArray.put(resultObject);
        }

        return resultArray;
    }*/

    public static JSONArray getMac(SNMPParams snmpParams) {
        // 按优先级获取 MAC 数据（getDeviceMac -> getDeviceMac2 -> getDeviceMac3）
        JSONObject macJson = getPriorityMacData(snmpParams);

        // 如果所有数据源都无效，返回空数组
        if (macJson == null || macJson.length() == 0) {
            return new JSONArray();
        }

        // 获取 MAC 类型和端口数据
        JSONObject macTypeJson = getMacTypeData(snmpParams);
        JSONObject portJson = getPortData(snmpParams);

        // 构建结果集
        JSONArray resultArray = new JSONArray();
        for (String mac : macJson.keySet()) {
            // 端口处理
            String portNumber = macJson.optString(mac, "0");
            String portName = portJson.optString(portNumber, "unknown");

            // MAC 类型处理（默认值为3）
            String macType = macTypeJson.optString(mac, "3");

            // 生成结果对象
            JSONObject entry = new JSONObject();
            entry.put("mac", mac);
            entry.put("port", portName);
            entry.put("type", macType);
            resultArray.put(entry);
        }
        return resultArray;
    }

    // --- 辅助方法 ---
    private static JSONObject getPriorityMacData(SNMPParams snmpParams) {
        // 按优先级顺序尝试获取 MAC 数据
        String[] methods = {"getDeviceMac2", "getDeviceMac3"};
        for (String method : methods) {
            try {
                String data = (String) SNMPv2Request.class
                        .getMethod(method, SNMPParams.class)
                        .invoke(null, snmpParams);
                if (isValidJson(data)) {
                    return new JSONObject(data);
                }
            } catch (Exception e) {
                // 静默处理反射异常，继续下一个方法
            }
        }
        return new JSONObject(); // 返回空对象
    }

    private static JSONObject getMacTypeData(SNMPParams snmpParams) {
        try {
            String data = SNMPv2Request.getDeviceMacType(snmpParams);
            return new JSONObject(data);
        } catch (JSONException e) {
            return new JSONObject(); // 返回空对象
        }
    }

    private static JSONObject getPortData(SNMPParams snmpParams) {
        try {
            String data = SNMPv2Request.getDevicePort(snmpParams);
            return new JSONObject(data);
        } catch (JSONException e) {
            return new JSONObject(); // 返回空对象
        }
    }

    private static boolean isValidJson(String data) {
        if (data == null || data.trim().isEmpty()) {
            return false;
        }
        try {
            JSONObject json = new JSONObject(data);
            return json.length() > 0;
        } catch (JSONException e) {
            return false;
        }
    }


    public static JSONArray getLldp(SNMPParams snmpParams) {
        // 获取 SNMP 数据
        String lldpData = SNMPv2Request.getLLDP(snmpParams);
        String lldpPortData = SNMPv2Request.getLLDPPort(snmpParams);

        // 解析 JSON 数据
        JSONObject lldpJson = new JSONObject(lldpData);
        JSONObject lldpPortJson = new JSONObject(lldpPortData);

        // 创建结果数组
        JSONArray resultArray = new JSONArray();

        // 遍历 LLDP 数据
        for (String lldpKey : lldpJson.keySet()) {
            String hostname = lldpJson.getString(lldpKey); // 获取主机名
            String remotePort = lldpPortJson.optString(lldpKey, "unknown"); // 获取对应的远程端口

            // 创建结果对象
            JSONObject resultObject = new JSONObject();
            resultObject.put("hostname", hostname);
            resultObject.put("remoteport", remotePort);

            // 添加到结果数组
            resultArray.put(resultObject);
        }

        return resultArray;
    }

    // dhcp
    public static String getDhcpStatus(){
        return DHCPUtil.getDhcpStatus();
    }

    // 根据type参数，指定获取dhcp或dhcp6进程状态
    public static String checkdhcpd(String type){
        return DHCPUtil.checkdhcpd(type);
    }

    public static String processOperation(String operation, String service){
        return DHCPUtil.processOperation(operation, service);
    }

    public static String modifyDHCP(String v4status, String v4int,
                                    String v6status, String v6int){
        DHCPUtil.modifyDHCP(v4status, v4int, v6status, v6int);
        return "modifyDHCP";
    }


    public static String getDnsSettings(){
        return DHCPUtil.getDnsSettings();
    }


    public static String modifyDns(String dns1, String dns2){
        DHCPUtil.modifyDNS(dns1,dns2);
        return "modifyDNS";
    }

    public static String getNetworkInterfaces(){
        return DHCPUtil.getNetworkInterfaces();
    }


    public static String modifyIp(String iface, String ipv4address, String ipv6address,
                                  String gateway4, String gateway6){
        return String.valueOf(DHCPUtil.modifyIp(iface,ipv4address,ipv6address,gateway4,gateway6));
    }

    public static String modifyVlans(String parentInterface, String vlanId, String ipv4Address,
                                     String ipv6Address, String gateway4,String gateway6){
        return String.valueOf(DHCPUtil.modifyVlans(parentInterface,vlanId,ipv4Address,ipv6Address,gateway4,gateway6));
    }

    public static String pingOp(String action, String service){
        return DHCPUtil.pingOp(action,service);
    }

    public static void pingTest(String network, int mask){
        DHCPUtil.pingSubnet(network,mask);
    }

    public static void pingSubnet(String network, int mask){
        DHCPUtil.pingSubnet(network,mask);
    }

    public static void pingSubnetConcurrent(String network, int mask){
        DHCPUtil.pingSubnetConcurrent(network,mask);
    }





}
