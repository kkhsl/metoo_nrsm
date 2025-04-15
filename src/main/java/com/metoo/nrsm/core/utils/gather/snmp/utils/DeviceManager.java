package com.metoo.nrsm.core.utils.gather.snmp.utils;

import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPV3Params;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.NetworkElement;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Component
public class DeviceManager {

    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private SnmpStatusUtils snmpStatusUtils;

    public void saveAvailableDevicesToRedis(){
        Set<String> hostNames = getDeviceNamesFromNetworkElement();
        // 更新redis
        this.snmpStatusUtils.editSnmpStatus(hostNames);
    }


    public Set<String> getDeviceNamesFromNetworkElement(){
        Set<String> hostNames = new HashSet<>();
        List<NetworkElement> networkElements = this.networkElementService.selectObjAllByGather();
        if(networkElements.size() > 0){
            for (NetworkElement element : networkElements) {
                String hostName = getDeviceNameByIpAndCommunityVersion(element);
                if(StringUtils.isNotEmpty(hostName)){
                    String key = element.getUuid();
                    hostNames.add(key);
                }
            }
        }
        return hostNames;
    }

    // 获取设备名
    public String getDeviceNameByIpAndCommunityVersion(NetworkElement networkElement){
//        SNMPParams snmpParams = new SNMPParams(element.getIp(), element.getVersion(), element.getCommunity());
        String result = SNMPv3Request.getDeviceName(SNMPParamFactory.createSNMPParam(networkElement));
        return result;
    }

    @Test
    public void test(){
        SNMPV3Params v2c = new SNMPV3Params.Builder()
                .version("v3")
                .host("192.168.6.1")
                .port(161)
                .community("public@123")
                .build();

    }

}
