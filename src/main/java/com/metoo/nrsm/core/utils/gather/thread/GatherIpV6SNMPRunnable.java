package com.metoo.nrsm.core.utils.gather.thread;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPRequest;
import com.metoo.nrsm.core.service.impl.Ipv6ServiceImpl;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.entity.Ipv4;
import com.metoo.nrsm.entity.Ipv6;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@Component
public class GatherIpV6SNMPRunnable implements Runnable{

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherIpV6SNMPRunnable() {
    }

    public GatherIpV6SNMPRunnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherIpV6SNMPRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }



    /**
     * When an object implementing interface <code>Runnable</code> is used
     * to create a thread, starting the thread causes the object's
     * <code>run</code> method to be called in that separately executing
     * thread.
     * <p>
     * The general contract of the method <code>run</code> is that it may
     * take any action whatsoever.
     *
     * @see Thread#run()
     */
    @Override
    public void run() {

        Ipv6ServiceImpl ipv6Service = (Ipv6ServiceImpl) ApplicationContextUtils.getBean("ipv6ServiceImpl");
        SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
        // 处理数据并返回结果
        try {
            JSONArray result = SNMPRequest.getArp(snmpParams);
            if(!result.isEmpty()) {
                // 使用 Jackson 将 JSON 字符串转换为 List<Ipv4>
                ObjectMapper objectMapper = new ObjectMapper();
                List<Ipv6> ipv6s = objectMapper.readValue(result.toString(), new TypeReference<List<Ipv6>>(){});
                if (ipv6s.size() > 0) {
                    ipv6s.forEach(e -> {
                        e.setDeviceIp(networkElement.getIp());
                        e.setDeviceName(networkElement.getDeviceName());
                        e.setAddTime(date);
                    });
                }
                ipv6Service.batchSaveGather(ipv6s);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if(latch != null){
                latch.countDown();
            }
        }
    }
}
