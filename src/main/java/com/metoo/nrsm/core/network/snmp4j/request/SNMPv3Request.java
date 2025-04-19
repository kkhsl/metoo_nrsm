package com.metoo.nrsm.core.network.snmp4j.request;

import com.metoo.nrsm.core.network.snmp4j.constants.SNMP_OID;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.response.SNMPDataParser;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.snmp4j.*;
import org.snmp4j.event.ResponseEvent;
import org.snmp4j.mp.MPv3;
import org.snmp4j.mp.SnmpConstants;
import org.snmp4j.security.*;
import org.snmp4j.smi.*;
import org.snmp4j.transport.DefaultUdpTransportMapping;

import java.io.IOException;
import java.security.Security;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

import static com.metoo.nrsm.core.network.snmp4j.response.SNMPDataParser.convertOidToMac;

/**
 * 设计线程安全方案
 */
public class SNMPv3Request {

    // 新增安全协议初始化
    static {
        Security.addProvider(new BouncyCastleProvider());
        SecurityProtocols.getInstance().addDefaultProtocols();
    }

    // 使用自定义Holder同时保存Snmp和TransportMapping
    private static class SnmpContext {
        Snmp snmp;
        TransportMapping<?> transport;
    }

    private static ThreadLocal<SnmpContext> threadContext = ThreadLocal.withInitial(() -> {
        try {
            SnmpContext context = new SnmpContext();
            // 显式创建并保存transport
            context.transport = new DefaultUdpTransportMapping();
            context.snmp = new Snmp(context.transport);

            // USM初始化（原逻辑保留）
            USM usm = new USM(SecurityProtocols.getInstance(),
                    new OctetString(MPv3.createLocalEngineID()), 0);
            SecurityModels.getInstance().addSecurityModel(usm);

            context.transport.listen();
            return context;
        } catch (IOException e) {
            System.err.println("SNMP传输层初始化失败: " + e.getMessage());
            return null;
        }
    });

    // 资源释放方法（不再依赖getTransportMapping）
    public static void cleanup() {
        SnmpContext context = threadContext.get();
        if (context != null) {
            try {
                // 显式关闭transport
                if (context.transport != null) {
                    context.transport.close();
                    //System.out.println("传输层已成功关闭");
                }
            } catch (IOException e) {
                //System.err.println("关闭传输层失败: " + e.getMessage());
            } finally {
                // 确保移除ThreadLocal引用
                threadContext.remove();
            }
        }
    }


    // 修改配置目标方法（支持v3）
    private static Target configureTarget(SNMPV3Params params) {
        Address targetAddress = GenericAddress.parse("udp:" + params.getHost() + "/" + params.getPort());

        if (params.getVersion().equals("v3")) {
            return configureV3Target(targetAddress, params);
        }
        return configureV2cTarget(targetAddress, params);
    }

    private static CommunityTarget configureV2cTarget(Address address, SNMPV3Params params) {
        CommunityTarget target = new CommunityTarget();
        target.setCommunity(new OctetString(params.getCommunity()));
        target.setAddress(address);
        target.setVersion(SnmpConstants.version2c);
        target.setTimeout(params.getTimeout());
        target.setRetries(params.getRetries());
        return target;
    }

    private static UserTarget configureV3Target(Address address, SNMPV3Params params) {
        UserTarget target = new UserTarget();
        target.setAddress(address);
        target.setVersion(SnmpConstants.version3);
        target.setSecurityLevel(params.getSecurityLevel());
        target.setSecurityName(new OctetString(params.getUsername()));
        target.setTimeout(params.getTimeout());
        target.setRetries(params.getRetries());
        return target;
    }

    // 新增用户安全配置
    private static synchronized void configureV3Security(SNMPV3Params params) {
        try {
            Snmp snmp = threadContext.get().snmp;
            USM usm = snmp.getUSM();

            OctetString securityName = new OctetString(params.getUsername());
            OctetString engineID = new OctetString(MPv3.createLocalEngineID());

            // 检查用户是否存在
            if (usm.getUser(engineID, securityName) == null) {
                UsmUser user = new UsmUser(
                        new OctetString(params.getUsername()),
                        resolveAuthProtocol(params.getAuthProtocol()),
                        params.getAuthPassword() != null ?
                                new OctetString(params.getAuthPassword()) :
                                new OctetString(),
                        resolvePrivProtocol(params.getPrivProtocol()),
                        params.getPrivPassword() != null ?
                                new OctetString(params.getPrivPassword()) :
                                new OctetString()
                );
                usm.addUser(securityName, user);
            }
        } catch (Exception e) {
            System.err.println("安全配置失败: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 协议解析方法
    private static OID resolveAuthProtocol(String protocol) {
        if (protocol == null) return null;
        switch (protocol.toUpperCase()) {
            case "SHA": return AuthSHA.ID;
            case "MD5": return AuthMD5.ID;
            default: throw new IllegalArgumentException("不支持的认证协议: " + protocol);
        }
    }

    private static OID resolvePrivProtocol(String protocol) {
        if (protocol == null) return null;
        switch (protocol.toUpperCase()) {
            case "AES": return PrivAES128.ID;
            case "DES": return PrivDES.ID;
            default: throw new IllegalArgumentException("不支持的加密协议: " + protocol);
        }
    }

    // 请求发送方法（支持v3）
    public static PDU sendRequest(SNMPV3Params params, SNMP_OID oid) {
        try {
            Snmp snmp = threadContext.get().snmp;
            if (snmp == null) {
                System.err.println("SNMP实例未初始化");
                return null;
            }

            if (params.getVersion().equals("v3")) {
                configureV3Security(params);
            }

            Target target = configureTarget(params);
            PDU pdu = createPDU(params, oid.getOid(), PDU.GET);

            ResponseEvent response = snmp.send(pdu, target);
            return processResponse(response);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            cleanup();
        }
    }


    public static PDU sendStrRequest(SNMPV3Params params, String oid) {
        try {
            Snmp snmp = threadContext.get().snmp;
            if (snmp == null) {
                System.err.println("SNMP实例未初始化");
                return null;
            }

            try {
                // 配置v3安全参数
                if (params.getVersion().equals("v3")) {
                    configureV3Security(params);
                }

                Target target = configureTarget(params);
                PDU pdu = createPDU(params, oid, PDU.GET);

                ResponseEvent response = snmp.send(pdu, target);
                return processResponse(response);
            } catch (Exception e) {
                System.err.println("请求异常: " + e.getClass().getSimpleName() + ": " + e.getMessage());
                e.printStackTrace();
                return null;
            }
        } finally {
            cleanup();
        }
    }

    // 创建PDU（支持v3的ScopedPDU）
    private static PDU createPDU(SNMPV3Params params, String oid, int pduType) {
        PDU pdu;
        if (params.getVersion().equals("v3")) {
            pdu = new ScopedPDU();
        } else {
            pdu = new PDU();
        }
        pdu.add(new VariableBinding(new OID(oid)));
        pdu.setType(pduType);
        return pdu;
    }

    // 修改GETNEXT方法
    private static Map<String, String> sendGETNEXTRequest(SNMPV3Params params, SNMP_OID snmpOid) {
        Map<String, String> resultMap = new HashMap<>();
        Snmp snmp = threadContext.get().snmp;
        if (snmp == null) {
            System.err.println("SNMP实例未初始化");
            return null;
        }

        try {
            if (params.getVersion().equals("v3")) {
                configureV3Security(params);
            }

            Target target = configureTarget(params);
            OID currentOid = new OID(snmpOid.getOid());

            while (true) {
                PDU pdu = createPDU(params, currentOid.toString(), PDU.GETNEXT);
                ResponseEvent response = snmp.send(pdu, target);
                PDU responsePDU = processResponse(response);

                if (responsePDU == null) break;

                VariableBinding vb = responsePDU.get(0);
                if (!vb.getOid().toString().startsWith(snmpOid.getOid())) {
                    break;
                }

                resultMap.put(vb.getOid().toString(), vb.getVariable().toString());
                currentOid = vb.getOid();
            }
        } catch (Exception e) {
            System.err.println("GETNEXT请求异常: " + e.getMessage());
            e.printStackTrace();
            return null;
        }finally {
            cleanup();
        }
        return resultMap;
    }

    private static PDU processResponse(ResponseEvent event) {
        if (event.getResponse() == null) {
            System.err.println("SNMP 实例初始化失败");
        }
        PDU response = event.getResponse();
        if (response == null) {
            System.err.println("无响应(超时或目标不可达) ");
        }
        if(response != null && response.getErrorStatus() != PDU.noError){
            System.err.println("无响应(超时或目标不可达) 或者SNMP 错误" + response.getErrorStatusText());
        }
        return response;
    }


    //解析 SNMP 响应，获取设备名
    public static String getDeviceName(SNMPV3Params snmpParams) {
        PDU responsePdu = sendRequest(snmpParams, SNMP_OID.HOST_NAME);
        return SNMPDataParser.parseDeviceName(responsePdu);
    }

    //解析 SNMP 响应，获取arp
    public static String getDeviceArp(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 ARP 表遍历
        Map<String, String> arpMap = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceArp(arpMap));
    }

    //获取 ARPV6 表、端口和端口映射
    public static JSONArray getArpV6(SNMPV3Params snmpParams) {
        Map<String, String> arpV6Map = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP_V6);
        String portData = SNMPv3Request.getDevicePort(snmpParams);
        JSONObject portJson = new JSONObject(portData);
        JSONArray resultArray = SNMPDataParser.parseDeviceArpV6(arpV6Map, portJson);
        return resultArray;
    }


    public static JSONArray getPortTableV6(SNMPV3Params snmpParams) {
        Map<String, String> portV6Map = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_IPV6);
        // 获取 SNMP 数据
        String portData = SNMPv3Request.getDevicePort(snmpParams);

        // 解析 JSON 数据
        JSONObject portJson = new JSONObject(portData);
        JSONArray resultArray = SNMPDataParser.parseDevicePortV6(portV6Map, portJson);

        return resultArray;
    }

    public static String getDeviceArpPort(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 ArpPort 表遍历
        Map<String, String> arpPortMap = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP_PORT);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceArpPort(arpPortMap));
    }

    public static String getDeviceArpV6(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 ArpV6 表遍历
        Map<String, String> arpV6Map = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP_V6);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceArpV6(arpV6Map));
    }

    public static String getDeviceArpV6Port(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 ArpV6Port 表遍历
        Map<String, String> arpV6PortMap = sendGETNEXTRequest(snmpParams, SNMP_OID.ARP_V6_PORT);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceArpPort(arpV6PortMap));
    }

    public static String getDevicePort(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 Port 表遍历
        Map<String, String> portMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT);
        /*Map<String, String> portMap = new HashMap<>();
        // 赋值
        portMap.put("52","GigabitEthernet1/0/52");
        portMap.put("12","GigabitEthernet1/0/12");*/
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePort(portMap));
    }

    public static String getDevicePortIp(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortIp 表遍历
        Map<String, String> portIpMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_IP);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePortIp(portIpMap));
    }

    public static String getDevicePortV6(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortV6 表遍历
        Map<String, String> portV6Map = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_IPV6);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePortV6(portV6Map));
    }

    public static String getDevicePortMask(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortMask 表遍历
        Map<String, String> portMaskMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_MASK);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePortMask(portMaskMap));
    }

    public static String getDevicePortDescription(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortDescription 表遍历
        Map<String, String> portDescriptionMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_DESCRIPTION);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDevicePortDescription(portDescriptionMap));
    }

    public static String getDeviceMac(SNMPV3Params snmpParams) {
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

    public static String getDeviceMac2(SNMPV3Params snmpParams) {
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
    public static String getDeviceMac3(SNMPV3Params snmpParams) {
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

    public static String getDevicePortStatus(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortStatus 表遍历
        Map<String, String> portStatusMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_STATUS);
        return SNMPDataParser.convertToJson(SNMPDataParser.parsePortStatus(portStatusMap));
    }

    public static String getDevicePortMac(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 PortMac 表遍历
        Map<String, String> portMacMap = sendGETNEXTRequest(snmpParams, SNMP_OID.PORT_MAC);
        return SNMPDataParser.convertToJson(SNMPDataParser.parsePortMac(portMacMap));
    }

    public static String getDeviceMacType(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 MacType 表遍历
        Map<String, String> macTypeMap = sendGETNEXTRequest(snmpParams, SNMP_OID.MAC_TYPE);
         /*Map<String, String> macTypeMap = new HashMap<>();
        // 赋值
        macTypeMap.put("1.3.6.1.2.1.17.4.3.1.3.0.11.95.228.214.24", "3");   //1.3.6.1.2.1.17.4.3.1.3.0.11.95.228.214.0   3
        macTypeMap.put("1.3.6.1.2.1.17.4.3.1.3.48.67.215.235.184.16", "3");*/
        String str = SNMP_OID.MAC_TYPE.getOid() + ".";
        return SNMPDataParser.convertToJson(SNMPDataParser.parseDeviceMacType(macTypeMap, str));
    }

    public static String getDeviceUpdateTime(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 MacType 表遍历
        PDU updateTimeMap = sendRequest(snmpParams, SNMP_OID.UP_TIME);
        return SNMPDataParser.parseDeviceUpdateTime(updateTimeMap);
    }

    public static String getV6Device(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 V6Device 表遍历
        Map<String, String> v6Device = sendGETNEXTRequest(snmpParams, SNMP_OID.IPV6_DEVICE);
        return SNMPDataParser.convertToJson(SNMPDataParser.parsePortMac(v6Device));
    }

    public static String getLLDP(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 LLDP 表遍历
        Map<String, String> lldp = sendGETNEXTRequest(snmpParams, SNMP_OID.LLDP);
        /*Map<String, String> lldp = new HashMap<>();
        // 赋值
        lldp.put("1.0.8802.1.1.2.1.4.1.1.9.8359.52.1", "zhihuigu_1L_HJ_SW");*/
        return SNMPDataParser.convertToJson(SNMPDataParser.parseLLDP(lldp));
    }

    public static String getLLDPPort(SNMPV3Params snmpParams) {
        // 调用 sendArpRequest 方法进行 SNMP 请求和 LLDP 表遍历
        Map<String, String> lldpPort = sendGETNEXTRequest(snmpParams, SNMP_OID.LLDP_ROMOTE_PORT);
        /*Map<String, String> lldpPort = new HashMap<>();
        // 赋值
        lldpPort.put("1.0.8802.1.1.2.1.4.1.1.7.8359.52.1", "GigabitEthernet1/0/1");
        lldpPort.put("1.0.8802.1.1.2.1.4.1.1.7.256479241.35.1", "28 D2 44 0F 08 DA");  //Hex-28D2440F08DA
        lldpPort.put("1.0.8802.1.1.2.1.4.1.1.7.256586179.43.1", "B8 88 E3 EB 40 AE");  //Hex-B888E3EB40AE*/
        return SNMPDataParser.convertToJson(SNMPDataParser.parseLLDPPort(lldpPort));
    }


    public static Boolean getIsV6(SNMPV3Params snmpParams) {
        // 调用 sendRequest 方法进行 SNMP 请求
        PDU isV6 = sendRequest(snmpParams, SNMP_OID.IS_IPV6);
        return SNMPDataParser.parseIsV6(isV6);
    }

    public static String getTraffic(SNMPV3Params snmpParams, String oid1, String oid2) {
        // 调用 sendArpRequest 方法进行 SNMP 请求
        PDU in = sendStrRequest(snmpParams, oid1);
        PDU out = sendStrRequest(snmpParams,oid2);
        return SNMPDataParser.convertToJson(SNMPDataParser.parseTraffic(in,out));
    }


    // 获取 ARP 表、端口和端口映射
    public static JSONArray getArp(SNMPV3Params snmpParams) {
        String arpData = SNMPv3Request.getDeviceArp(snmpParams);
        String arpPortData = SNMPv3Request.getDeviceArpPort(snmpParams);
        String portData = SNMPv3Request.getDevicePort(snmpParams);
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

    public static JSONArray getPortMac(SNMPV3Params snmpParams) {
        // 获取 SNMP 数据
        String macData = SNMPv3Request.getDevicePortMac(snmpParams);
        String statusData = SNMPv3Request.getDevicePortStatus(snmpParams);
        String portData = SNMPv3Request.getDevicePort(snmpParams);


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

    public static JSONArray getPortTable(SNMPV3Params snmpParams) {
        // 获取 SNMP 数据
        String portData = SNMPv3Request.getDevicePort(snmpParams);
        String statusData = SNMPv3Request.getDevicePortStatus(snmpParams);
        String portIpData = SNMPv3Request.getDevicePortIp(snmpParams);
        String portMaskData = SNMPv3Request.getDevicePortMask(snmpParams);
        String portDescriptionData = SNMPv3Request.getDevicePortDescription(snmpParams);

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

    public static JSONArray getMac(SNMPV3Params snmpParams) {
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
    private static JSONObject getPriorityMacData(SNMPV3Params snmpParams) {
        // 按优先级顺序尝试获取 MAC 数据
        String[] methods = {"getDeviceMac", "getDeviceMac2", "getDeviceMac3"};
        for (String method : methods) {
            try {
                String data = (String) SNMPv3Request.class
                        .getMethod(method, SNMPV3Params.class)
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

    private static JSONObject getMacTypeData(SNMPV3Params snmpParams) {
        try {
            String data = SNMPv3Request.getDeviceMacType(snmpParams);
            return new JSONObject(data);
        } catch (JSONException e) {
            return new JSONObject(); // 返回空对象
        }
    }

    private static JSONObject getPortData(SNMPV3Params snmpParams) {
        try {
            String data = SNMPv3Request.getDevicePort(snmpParams);
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


    public static JSONArray getLldp(SNMPV3Params snmpParams) {
        // 获取 SNMP 数据
        String lldpData = SNMPv3Request.getLLDP(snmpParams);
        String lldpPortData = SNMPv3Request.getLLDPPort(snmpParams);

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

}
