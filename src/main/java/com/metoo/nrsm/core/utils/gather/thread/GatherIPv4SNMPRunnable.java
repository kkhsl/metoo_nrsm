package com.metoo.nrsm.core.utils.gather.thread;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.entity.Ipv4;
import com.metoo.nrsm.entity.NetworkElement;
import org.json.JSONArray;

import java.util.Date;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
//@Data
//@Component
public class GatherIPv4SNMPRunnable implements Runnable{

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;


    public GatherIPv4SNMPRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }

    @Override
    public void run() {
        Ipv4Service ipv4Service = (Ipv4Service) ApplicationContextUtils.getBean("ipv4ServiceImpl");
        SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
        // 处理数据并返回结果
        try {
//            JSONArray result = SNMPv2Request.getArp(snmpParams);
            JSONArray result = SNMPv3Request.getArp(SNMPParamFactory.createSNMPParam(networkElement));

            if(!result.isEmpty()) {
                // 使用 Jackson 将 JSON 字符串转换为 List<Ipv4>
                ObjectMapper objectMapper = new ObjectMapper();
                List<Ipv4> ipv4s = objectMapper.readValue(result.toString(), new TypeReference<List<Ipv4>>(){});
                if (ipv4s != null && ipv4s.size() > 0) {
                    ipv4s.forEach(e -> {
                        e.setDeviceIp(networkElement.getIp());
                        e.setDeviceName(networkElement.getDeviceName());
                        e.setAddTime(date);
                    });
                    ipv4Service.batchSaveGather(ipv4s);
                }
            }
        } catch (Exception e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
        } finally {
            if(latch != null){
                latch.countDown();
            }
        }
    }
}
