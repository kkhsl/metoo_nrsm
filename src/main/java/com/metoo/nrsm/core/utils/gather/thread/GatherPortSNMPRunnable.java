package com.metoo.nrsm.core.utils.gather.thread;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.impl.PortServiceImpl;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.Port;
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
public class GatherPortSNMPRunnable implements Runnable{


    private NetworkElement networkElement;
    private Date date;
    private CountDownLatch latch;

    public GatherPortSNMPRunnable() {
    }

    public GatherPortSNMPRunnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherPortSNMPRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }


    @Override
    public void run() {
        try {
            PortServiceImpl portService = (PortServiceImpl) ApplicationContextUtils.getBean("portServiceImpl");
//            SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
//            JSONArray result = SNMPv2Request.getPortTable(snmpParams);
            JSONArray result = SNMPv3Request.getPortTable(SNMPParamFactory.createSNMPParam(networkElement));

            if(!result.isEmpty()){
                // 使用 Jackson 将 JSON 字符串转换为 List<Ipv4>
                ObjectMapper objectMapper = new ObjectMapper();
                List<Port> ports = objectMapper.readValue(result.toString(), new TypeReference<List<Port>>(){});
                ports.forEach(e -> {
                    e.setDeviceUuid(networkElement.getUuid());
                    e.setAddTime(date);
                });
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
