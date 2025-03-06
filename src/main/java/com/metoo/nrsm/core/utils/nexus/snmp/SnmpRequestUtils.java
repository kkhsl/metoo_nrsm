package com.metoo.nrsm.core.utils.nexus.snmp;


import com.alibaba.fastjson.JSONObject;
import com.metoo.sdk.SNMPSDK;
import com.metoo.utils.SNMPException;

public class SnmpRequestUtils {


    public static void main(String[] args) throws SNMPException {
        SNMPSDK sdk = new SNMPSDK();

        Object get_ipv4_port = sdk.operateV2C(
                "gw",
                "240e:380:2:42ba:5a48:496c:5a29:bc10",
                "read@public",
                null,
                "1.3.6.1.2.1.1.5.0",
                "abt",
                "test"
        );
        System.out.println(JSONObject.toJSONString(get_ipv4_port));

    }

}
