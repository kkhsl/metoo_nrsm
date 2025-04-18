package com.metoo.nrsm.core.utils.gather.thread;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.impl.PortIpv6ServiceImpl;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.PortIpv6;
import org.json.JSONArray;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Component
public class GatherPortIpv6SNMPRunnable implements Runnable{


    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherPortIpv6SNMPRunnable() {
    }

    public GatherPortIpv6SNMPRunnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherPortIpv6SNMPRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
//            SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
//            JSONArray result = SNMPv2Request.getPortTable(snmpParams);
            JSONArray result = SNMPv3Request.getPortTableV6(SNMPParamFactory.createSNMPParam(networkElement));
            if(result != null && result.length() > 0){
                ObjectMapper objectMapper = new ObjectMapper();
                List<PortIpv6> ports = objectMapper.readValue(result.toString(), new TypeReference<List<PortIpv6>>(){});
                ports.forEach(e -> {
                    e.setDeviceUuid(networkElement.getUuid());
                    e.setAddTime(date);
                });
                PortIpv6ServiceImpl portService = (PortIpv6ServiceImpl) ApplicationContextUtils.getBean("portIpv6ServiceImpl");
                portService.batchSaveGather(ports);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if(latch != null){
                latch.countDown();
            }
        }
    }
}
