package com.metoo.nrsm.core.network.snmp4j.request;

import com.metoo.nrsm.core.network.snmp4j.constants.SNMP_OID;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.response.SNMPDataParser;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.junit.Test;
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
public class SNMPRequest {

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

            if (responsePDU == null || (responsePDU != null && responsePDU.getErrorStatus() != PDU.noError)) {
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

            if (responsePDU == null || responsePDU.getErrorStatus() != PDU.noError) {
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
        // 调用 sendArpRequest 方法进行 SNMP 请求和 MacType 表遍历
        PDU isV6 = sendRequest(snmpParams, SNMP_OID.UP_TIME);
        return SNMPDataParser.parseIsV6(isV6);
    }


    // 获取 ARP 表、端口和端口映射
    public static JSONArray getArp(SNMPParams snmpParams) {
        String arpData = SNMPRequest.getDeviceArp(snmpParams);
        String arpPortData = SNMPRequest.getDeviceArpPort(snmpParams);
        String portData = SNMPRequest.getDevicePort(snmpParams);
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

    public static JSONArray getPortMac(SNMPParams snmpParams) {
        // 获取 SNMP 数据
        String macData = SNMPRequest.getDevicePortMac(snmpParams);
        String statusData = SNMPRequest.getDevicePortStatus(snmpParams);
        String portData = SNMPRequest.getDevicePort(snmpParams);


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
        String portData = SNMPRequest.getDevicePort(snmpParams);
        String statusData = SNMPRequest.getDevicePortStatus(snmpParams);
        String portIpData = SNMPRequest.getDevicePortIp(snmpParams);
        String portMaskData = SNMPRequest.getDevicePortMask(snmpParams);
        String portDescriptionData = SNMPRequest.getDevicePortDescription(snmpParams);

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
        String macData = SNMPRequest.getDeviceMac3(snmpParams);
        String macTypeData = SNMPRequest.getDeviceMacType(snmpParams);
        String portData = SNMPRequest.getDevicePort(snmpParams);

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
        String[] methods = {"getDeviceMac", "getDeviceMac2", "getDeviceMac3"};
        for (String method : methods) {
            try {
                String data = (String) SNMPRequest.class
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
            String data = SNMPRequest.getDeviceMacType(snmpParams);
            return new JSONObject(data);
        } catch (JSONException e) {
            return new JSONObject(); // 返回空对象
        }
    }

    private static JSONObject getPortData(SNMPParams snmpParams) {
        try {
            String data = SNMPRequest.getDevicePort(snmpParams);
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
        String lldpData = SNMPRequest.getLLDP(snmpParams);
        String lldpPortData = SNMPRequest.getLLDPPort(snmpParams);

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

    @Test
    public void test(){
        String str = "[{\"mac\": \"00:0b:5f:e4:d6:00\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"00:0b:5f:e4:d6:18\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:00:84:0b\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:00:be:58\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:01:8f:f6\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:02:b6:50\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:03:7e:3c\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:04:1b:08\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:04:6a:09\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:04:94:f7\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:04:9e:ef\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:04:b6:1b\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:08:3c:17\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:0b:98:68\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:0b:db:ca\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:10:3c:70\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:10:56:03\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:11:51:6b\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:13:6d:a8\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:14:a3:70\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:15:5a:1d\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:16:6c:33\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:16:76:4b\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:18:4a:93\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:18:bb:48\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:1a:0c:36\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:1a:2d:00\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:1b:06:05\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:1b:27:55\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:1b:f2:5c\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:1f:18:a7\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:1f:1d:d6\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:20:e6:f0\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:24:ae:e7\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:26:10:73\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:26:25:96\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:26:8e:a5\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:27:f4:cd\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:28:b9:fc\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:29:49:c5\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:2a:15:30\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:2a:ce:bd\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:2b:15:43\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:2b:1d:e8\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:2b:5f:08\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:2d:45:42\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:2d:75:64\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:2d:fb:df\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:30:5d:99\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:31:b0:71\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:33:2a:e7\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:39:2c:12\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:3b:30:09\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:3f:65:cc\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:3f:ea:b3\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:41:77:99\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:41:77:e8\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:44:81:17\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:44:b2:01\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:44:cf:5e\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:46:be:1e\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:48:33:70\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:4b:8b:e2\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:4b:e2:36\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:4c:a7:e9\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:4e:cc:ed\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:50:0d:9d\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:50:fa:24\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:52:6d:9d\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:54:80:f4\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:56:2e:96\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:56:34:4a\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:57:08:1e\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:57:08:28\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:57:08:32\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:59:7c:be\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:5b:52:14\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:5c:5f:95\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:5c:5f:9f\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:5c:97:6a\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:5c:d8:a7\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:5d:f3:e7\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:5f:84:40\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:5f:e3:d6\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:60:30:b0\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:62:24:92\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:63:24:26\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:64:9b:61\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:66:3f:a1\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:66:bc:ae\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:67:04:c6\", \"port\": \"null\", \"type\": \"1\"}, {\"mac\": \"00:0c:29:69:43:47\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:6c:96:84\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:6f:0c:f2\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:74:ea:1c\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:76:34:8c\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:76:95:32\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:77:8d:00\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:77:be:6f\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:78:1e:e2\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:78:60:d4\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:78:b0:f8\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:7a:0d:db\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:7b:c1:5f\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:7c:73:e2\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:7c:7f:51\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:7c:bb:a4\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:7d:83:b1\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:7e:b8:8c\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:7f:1c:f8\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:82:47:16\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:85:3e:49\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:85:93:01\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:86:b6:49\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:87:2f:cb\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:87:31:7e\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:87:3b:b6\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:88:d0:2a\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:89:20:0b\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:8c:36:81\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:8c:f7:73\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:8c:f7:7d\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:8e:80:87\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:90:98:a8\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:90:fb:d9\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:93:24:ac\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:95:2f:ec\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:95:3b:4b\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:96:9c:6e\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:9b:4d:8e\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:9c:55:26\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:9c:93:a0\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:9d:35:1b\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:9d:84:3c\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:a4:ab:09\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:a6:88:5d\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:a6:ef:86\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:a7:37:cd\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:a8:02:f8\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:a8:2a:33\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:a9:54:2d\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:a9:d0:74\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:aa:8d:fa\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ae:c9:f8\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b1:fc:34\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b2:6c:96\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b3:20:cf\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b4:c4:fe\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b5:cb:ba\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b5:eb:5b\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b6:14:da\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b6:57:27\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:b6:b1:e0\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ba:13:7b\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ba:23:91\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:bb:3f:98\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:bc:09:f3\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:bc:d2:88\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:bd:ae:f1\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:be:02:d3\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:be:41:cf\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:be:a1:92\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c0:0e:3d\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c0:d0:8e\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c1:4e:0f\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c2:3e:4b\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c2:8c:cb\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c3:70:43\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c4:75:fb\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c4:97:2e\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c7:8a:c0\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:c7:db:02\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:cb:81:1c\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:cd:77:4e\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ce:13:d6\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d0:6f:fc\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d0:72:60\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d1:2e:41\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d2:5f:03\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d3:b6:8e\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d5:61:7c\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d6:a1:08\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d8:e7:83\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d9:24:a1\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:d9:7e:15\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:db:a1:69\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:dc:06:2f\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:dd:7b:ce\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:e2:1c:2b\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:e3:8e:e4\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:e4:ee:82\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:e7:19:7b\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:e7:73:8e\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ea:dc:9a\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ec:12:65\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ec:85:a2\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ed:cd:30\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ee:19:cc\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ef:74:e3\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:f1:be:b1\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:f2:c3:73\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:f2:ce:9d\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:f6:05:e9\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:f8:8f:f1\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:f8:f9:44\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:fb:59:88\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:fc:f5:9b\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0c:29:ff:57:e4\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:0e:c6:c7:b8:8d\", \"port\": \"GigabitEthernet3/0/22\", \"type\": \"3\"}, {\"mac\": \"00:0f:e2:49:d3:76\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"00:12:7f:23:91:00\", \"port\": \"GigabitEthernet3/0/26\", \"type\": \"3\"}, {\"mac\": \"00:12:7f:23:91:18\", \"port\": \"GigabitEthernet3/0/26\", \"type\": \"3\"}, {\"mac\": \"00:14:5e:f0:8d:48\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"00:14:69:ae:2e:40\", \"port\": \"GigabitEthernet3/0/20\", \"type\": \"3\"}, {\"mac\": \"00:14:69:ae:2e:58\", \"port\": \"GigabitEthernet3/0/20\", \"type\": \"3\"}, {\"mac\": \"00:1a:64:e5:ee:14\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:1e:0b:bf:0b:38\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:21:5e:b1:0a:f0\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"00:22:64:05:77:1e\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"00:22:64:07:0f:a8\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"00:24:13:ec:be:b2\", \"port\": \"GigabitEthernet3/0/25\", \"type\": \"3\"}, {\"mac\": \"00:24:13:ec:be:c0\", \"port\": \"GigabitEthernet3/0/25\", \"type\": \"3\"}, {\"mac\": \"00:24:f9:fc:fb:32\", \"port\": \"GigabitEthernet3/0/16\", \"type\": \"3\"}, {\"mac\": \"00:24:f9:fc:fb:40\", \"port\": \"GigabitEthernet3/0/16\", \"type\": \"3\"}, {\"mac\": \"00:50:56:5c:26:88\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"00:50:56:61:b5:ee\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:14:64\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:4c:aa\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:6e:38\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:6f:26\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:83:a2\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:98:fe\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:99:8e\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:a4:b4\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:a8:ca\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:b6:88\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:b8:78\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:cd:aa\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:ce:af\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:da:15\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:e8:d0\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:e9:e4\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:ed:e4\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:86:f5:fd\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"00:50:56:a8:1e:ee\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:50:56:a8:42:07\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:50:56:a8:52:3e\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:50:56:a8:bd:88\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:50:56:a8:dd:1a\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"00:dd:b6:26:7b:a1\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:dd:b6:26:7b:bf\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"00:e0:4c:68:03:e0\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"00:e0:4c:68:08:5f\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"02:01:00:00:00:00\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"04:d9:c8:5f:04:d4\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"04:d9:c8:5f:05:25\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"04:d9:c8:5f:15:13\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"08:00:27:c6:95:15\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"08:26:ae:39:83:a1\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"08:94:ef:7e:21:e4\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"08:94:ef:7e:22:04\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"08:94:ef:7e:22:11\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"08:94:ef:7e:22:8c\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"08:94:ef:9d:3c:e2\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"10:19:65:67:0a:02\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"10:98:19:2d:6b:d3\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"10:9f:4f:16:30:4d\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"14:7d:da:a3:f0:77\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"18:ef:63:23:90:32\", \"port\": \"GigabitEthernet3/0/15\", \"type\": \"3\"}, {\"mac\": \"18:ef:63:23:90:40\", \"port\": \"GigabitEthernet3/0/15\", \"type\": \"3\"}, {\"mac\": \"1c:69:7a:4a:0d:ea\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"1c:69:7a:4a:e7:bf\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"1c:69:7a:53:1d:e3\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"1c:69:7a:54:3f:91\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"1c:98:ec:13:66:1c\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"22:20:fd:cf:fd:a1\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"24:28:fd:2d:ff:99\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"28:d2:44:75:b1:2a\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"28:d2:44:80:d6:b2\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"28:d2:44:a5:74:a0\", \"port\": \"GigabitEthernet3/0/20\", \"type\": \"3\"}, {\"mac\": \"28:d2:44:a5:76:62\", \"port\": \"GigabitEthernet3/0/20\", \"type\": \"3\"}, {\"mac\": \"28:d2:44:a5:7c:4f\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"28:d2:44:a5:83:b1\", \"port\": \"GigabitEthernet3/0/20\", \"type\": \"3\"}, {\"mac\": \"2c:97:b1:72:77:3c\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"2c:fd:a1:8c:87:80\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:b6\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c1\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c2\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c3\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c4\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c5\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c6\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c7\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c8\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:c9\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:ca\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:0c:29:e7:b2:cb\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"30:24:a9:f9:95:18\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"30:29:52:24:0a:d8\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"30:5f:77:a1:b8:73\", \"port\": \"GigabitEthernet3/0/33\", \"type\": \"3\"}, {\"mac\": \"30:5f:77:a1:b8:a9\", \"port\": \"GigabitEthernet3/0/33\", \"type\": \"3\"}, {\"mac\": \"30:5f:77:fd:82:61\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"30:80:9b:5a:92:4f\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"30:80:9b:5a:93:cf\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"30:80:9b:5a:9a:8f\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"30:e1:71:60:bd:f5\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"34:ca:81:ea:3a:6f\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"34:dc:99:33:60:56\", \"port\": \"GigabitEthernet3/0/21\", \"type\": \"3\"}, {\"mac\": \"34:dc:99:33:60:88\", \"port\": \"GigabitEthernet3/0/21\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:2f:4e:40\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:2f:4f:80\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:2f:53:60\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:2f:54:f8\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:2f:57:40\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:2f:5a:60\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:2f:5c:b0\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:83:47:f8\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"38:68:dd:86:6a:28\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"3c:1b:f8:c4:bf:06\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"3c:1b:f8:c4:bf:0d\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"3c:c7:86:a1:91:e2\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"3c:cd:57:32:d1:55\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"42:2d:59:f0:d3:e9\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"44:a6:42:42:38:61\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"4c:cc:6a:dd:f8:88\", \"port\": \"GigabitEthernet3/0/15\", \"type\": \"3\"}, {\"mac\": \"52:54:00:2e:20:fb\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"52:54:00:6d:b0:a4\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"54:ab:3a:3c:cc:bd\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"54:f6:c5:f4:32:9a\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"58:66:ba:7f:89:7e\", \"port\": \"GigabitEthernet3/0/27\", \"type\": \"3\"}, {\"mac\": \"5c:c9:99:59:03:2f\", \"port\": \"GigabitEthernet3/0/29\", \"type\": \"3\"}, {\"mac\": \"5c:c9:99:59:03:69\", \"port\": \"GigabitEthernet3/0/29\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:67:d4:22\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:67:d4:74\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:04:c9\", \"port\": \"GigabitEthernet3/0/24\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:05:1b\", \"port\": \"GigabitEthernet3/0/24\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:06:a9\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:06:fb\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:08:89\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:08:db\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:16:c9\", \"port\": \"GigabitEthernet3/0/16\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:17:18\", \"port\": \"GigabitEthernet3/0/16\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:17:89\", \"port\": \"GigabitEthernet3/0/22\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:17:db\", \"port\": \"GigabitEthernet3/0/22\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:1e:a9\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"5c:dd:70:77:1e:fb\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"6c:02:e0:c1:6a:71\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"6c:3c:8c:66:e5:32\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:17:80\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:1b:50\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:24:20\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:24:f0\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:39:30\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:3b:50\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:43:20\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:43:70\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:43:c0\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6c:fe:54:0a:45:00\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"6e:4b:45:cf:44:3b\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"70:f9:6d:79:44:6b\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"70:f9:6d:79:44:bd\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"70:f9:6d:79:4e:2b\", \"port\": \"GigabitEthernet3/0/11\", \"type\": \"3\"}, {\"mac\": \"70:f9:6d:79:4e:7d\", \"port\": \"GigabitEthernet3/0/11\", \"type\": \"3\"}, {\"mac\": \"70:f9:6d:79:50:0b\", \"port\": \"GigabitEthernet3/0/13\", \"type\": \"3\"}, {\"mac\": \"70:f9:6d:79:50:5d\", \"port\": \"GigabitEthernet3/0/13\", \"type\": \"3\"}, {\"mac\": \"74:25:8a:fa:f2:1c\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"74:25:8a:fa:f2:6e\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"74:27:ea:c6:d0:75\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"74:5a:aa:49:f8:19\", \"port\": \"GigabitEthernet3/0/34\", \"type\": \"3\"}, {\"mac\": \"74:85:c4:37:80:67\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"74:85:c4:37:80:9d\", \"port\": \"GigabitEthernet3/0/30\", \"type\": \"3\"}, {\"mac\": \"74:85:c4:37:c9:87\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"74:85:c4:37:c9:bd\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"74:86:e2:06:f6:b5\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"78:1d:ba:a4:09:56\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"78:1d:ba:a4:09:57\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"78:1d:ba:a4:e4:21\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"78:2b:cb:3e:dc:a4\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"78:2b:cb:3e:dc:ac\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"78:e7:d1:e7:d9:aa\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"7c:8a:e1:17:bd:28\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"7c:d3:0a:67:79:8b\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"80:96:21:c3:68:01\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"80:96:21:c3:68:31\", \"port\": \"GigabitEthernet3/0/31\", \"type\": \"3\"}, {\"mac\": \"80:be:af:52:4e:47\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"80:be:af:f6:f3:b1\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"80:be:af:f6:fc:73\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"80:be:af:f6:fc:7f\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"84:2b:2b:4d:05:c2\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"84:2b:2b:4d:05:ca\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"88:d7:f6:d4:fd:7c\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"88:d7:f6:d5:00:16\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"8c:16:45:8d:c1:0a\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"90:97:b5:1a:f2:4d\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"90:b1:1c:04:0c:f5\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"90:b1:1c:06:fa:c3\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"92:b1:d2:f7:84:23\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"94:18:82:88:60:c0\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"94:18:82:89:df:58\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"9c:1d:36:e9:02:6d\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"a0:36:9f:68:72:5a\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"a0:36:9f:68:73:ea\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"a4:ae:12:81:65:7c\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"a4:ae:12:82:d2:57\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"a6:b0:45:c2:e2:f0\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"a8:37:59:c4:bd:3a\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"aa:1c:04:0e:a3:86\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:61:12\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:74\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:77\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:7a\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:7b\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:93\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:b2\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:bb\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:bc\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:be\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:c6\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:c7\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:cb\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:ce\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:d0\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:d4\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:d5\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:d9\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:da\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"ac:b9:2f:6b:64:db\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"b0:44:14:82:c3:f2\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"bc:e9:2f:84:41:85\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"c0:3f:d5:99:45:31\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"c0:3f:d5:e3:f7:dc\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"c0:3f:d5:e3:fe:a5\", \"port\": \"GigabitEthernet3/0/15\", \"type\": \"3\"}, {\"mac\": \"c4:65:16:b4:2f:a7\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"c4:65:16:b4:2f:d3\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"c8:1f:66:d3:72:7f\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"c8:1f:66:d3:79:ae\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"d0:50:99:dc:4f:39\", \"port\": \"GigabitEthernet3/0/41\", \"type\": \"3\"}, {\"mac\": \"d0:50:99:f2:da:f3\", \"port\": \"GigabitEthernet3/0/41\", \"type\": \"3\"}, {\"mac\": \"d4:3d:7e:ad:d8:d5\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"d4:ae:52:a6:6e:9e\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"d4:ae:52:a6:6e:a0\", \"port\": \"GigabitEthernet3/0/5\", \"type\": \"3\"}, {\"mac\": \"d8:43:ae:96:67:66\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"dc:4a:3e:86:1a:80\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"dc:4a:3e:88:47:78\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"dc:a6:32:41:74:30\", \"port\": \"GigabitEthernet3/0/19\", \"type\": \"3\"}, {\"mac\": \"e0:be:03:1e:34:c9\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"e0:be:03:87:8f:89\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"e4:a8:b6:9f:45:e6\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"e4:aa:ea:9f:c1:c9\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"e8:61:1f:18:5d:cd\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"e8:61:1f:18:66:8c\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"e8:61:1f:18:66:b2\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"e8:61:1f:18:67:24\", \"port\": \"GigabitEthernet3/0/3\", \"type\": \"3\"}, {\"mac\": \"e8:61:1f:21:2c:ec\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"e8:61:1f:21:3f:ce\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"e8:61:1f:21:3f:f0\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"e8:61:1f:21:40:64\", \"port\": \"GigabitEthernet3/0/32\", \"type\": \"3\"}, {\"mac\": \"e8:80:88:08:ed:cb\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"ec:26:ca:8b:24:b5\", \"port\": \"GigabitEthernet3/0/23\", \"type\": \"3\"}, {\"mac\": \"f0:63:f9:6f:3a:10\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"f4:03:43:49:8f:08\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}, {\"mac\": \"f4:6b:8c:97:93:e3\", \"port\": \"GigabitEthernet3/0/6\", \"type\": \"3\"}, {\"mac\": \"f8:a2:6d:2d:fe:54\", \"port\": \"GigabitEthernet3/0/12\", \"type\": \"3\"}, {\"mac\": \"f8:e4:3b:79:47:2e\", \"port\": \"GigabitEthernet3/0/15\", \"type\": \"3\"}, {\"mac\": \"fc:45:5f:21:69:78\", \"port\": \"GigabitEthernet3/0/48\", \"type\": \"3\"}, {\"mac\": \"fe:39:1d:5a:56:7a\", \"port\": \"GigabitEthernet3/0/4\", \"type\": \"3\"}]";
        System.out.println();
    }

}
