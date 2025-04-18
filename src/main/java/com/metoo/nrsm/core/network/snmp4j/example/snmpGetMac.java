package com.metoo.nrsm.core.network.snmp4j.example;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.Test;

@Slf4j
public class snmpGetMac {

    public static void main(String[] args) {
        String host = "192.168.0.40";  // 目标设备地址
        String version = "v2c";       // SNMP 版本
        String community = "transfar@123";  // SNMP 社区字符串
        SNMPParams snmpParams = new SNMPParams(host, version, community);
        // 处理数据并返回结果
        JSONArray result = SNMPv2Request.getMac(snmpParams);
        System.out.println(result.toString());
    }



    @Test
    public void getArpV6() {
        SNMPV3Params snmpv3Params = new SNMPV3Params.Builder()
                .version("v2c")
                .host("192.168.0.40")
                .port(161)
                .community("transfar@123")
                .build();

//        JSONArray result = SNMPv3Request.getMac(snmpv3Params);
//        log.info("arpV6:{}", result);

        String data = "{\"00:0c:29:1f:18:a7\":\"3\",\"00:0c:29:a4:ab:09\":\"3\",\"00:0c:29:c1:4e:0f\":\"3\",\"00:0c:29:2b:15:43\":\"1\",\"00:0c:29:1b:f2:5c\":\"1\",\"30:80:9b:5a:92:4f\":\"28\",\"00:0c:29:88:d0:2a\":\"3\",\"00:0c:29:e2:1c:2b\":\"3\",\"00:0c:29:1a:2d:00\":\"4\",\"00:0c:29:39:2c:12\":\"4\",\"00:0c:29:54:80:f4\":\"4\",\"00:14:69:ae:2e:58\":\"28\",\"00:0c:29:69:43:47\":\"1\",\"00:0c:29:76:34:8c\":\"4\",\"00:0c:29:aa:8d:fa\":\"4\",\"00:0c:29:bc:09:f3\":\"4\",\"00:0c:29:44:b2:01\":\"3\",\"00:0c:29:cb:81:1c\":\"3\",\"34:73:5a:9d:51:bf\":\"4\",\"00:0c:29:02:b6:50\":\"4\",\"00:0c:29:33:2a:e7\":\"3\",\"00:0c:29:6f:0c:f2\":\"3\",\"00:12:7f:23:91:18\":\"28\",\"5c:c9:99:59:03:69\":\"28\",\"00:0c:29:59:7c:be\":\"4\",\"52:54:00:6d:b0:a4\":\"2\",\"00:0c:29:b1:fc:34\":\"3\",\"38:68:dd:3a:4a:0a\":\"3\",\"00:0c:29:87:2f:cb\":\"4\",\"18:ef:63:23:90:32\":\"28\",\"34:b3:54:10:8d:70\":\"28\",\"00:0c:29:c2:3e:4b\":\"4\",\"00:0c:29:04:b6:1b\":\"4\",\"30:5f:77:a1:b8:73\":\"28\",\"00:0c:29:5c:97:6a\":\"3\",\"00:0c:29:d6:a1:08\":\"4\",\"00:0f:e2:07:f2:e0\":\"28\",\"34:b3:54:10:8d:30\":\"28\",\"00:24:13:ec:be:c0\":\"28\",\"00:0c:29:66:3f:a1\":\"3\",\"2c:fd:a1:8c:87:80\":\"2\",\"00:0c:29:8c:36:81\":\"3\",\"74:25:8a:fa:f2:1c\":\"28\",\"00:0c:29:b5:cb:ba\":\"1\",\"2c:97:b1:72:77:3c\":\"1\",\"00:0c:29:ef:74:e3\":\"1\",\"00:0c:29:18:bb:48\":\"4\",\"00:0c:29:c3:70:43\":\"3\",\"00:24:13:ec:be:b2\":\"28\",\"00:0b:5f:e4:d6:18\":\"28\",\"00:0c:29:7c:73:e2\":\"4\",\"00:0c:29:be:a1:92\":\"4\",\"00:0c:29:1b:27:55\":\"4\",\"74:5a:aa:49:f8:19\":\"2\",\"74:85:c4:37:80:67\":\"28\",\"00:0c:29:5c:d8:a7\":\"3\",\"00:0c:29:e7:19:7b\":\"4\",\"52:54:00:2e:20:fb\":\"2\",\"74:85:c4:37:c9:87\":\"28\",\"52:54:00:e9:bd:05\":\"2\",\"00:0c:29:04:9e:ef\":\"4\",\"00:0c:29:b6:14:da\":\"3\",\"00:0c:29:b3:20:cf\":\"1\",\"34:73:5a:9d:da:4c\":\"10\",\"00:0c:29:cd:77:4e\":\"4\",\"00:0c:29:d0:72:60\":\"1\",\"5c:dd:70:ee:30:56\":\"28\",\"00:0c:29:04:93:4a\":\"4\",\"00:0c:29:76:95:32\":\"1\",\"00:24:f9:fc:fb:32\":\"28\"}";
        JSONObject jsonObject = new JSONObject(data);
        for (String mac : jsonObject.keySet()) {
            // 端口处理
            String portNumber = jsonObject.optString(mac, "0");
            System.out.println(portNumber);
        }
    }

}