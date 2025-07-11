package com.metoo.nrsm.core.utils.gather.thread;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.network.snmp4j.param.SNMPParams;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPParamFactory;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv2Request;
import com.metoo.nrsm.core.network.snmp4j.request.SNMPv3Request;
import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.entity.NetworkElement;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Date;
import java.util.concurrent.CountDownLatch;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 11:36
 */
@Slf4j
@Component
public class GatherIsIpv6SNMPRunnable implements Runnable {

    private NetworkElement networkElement;

    private Date date;

    private CountDownLatch latch;

    public GatherIsIpv6SNMPRunnable() {
    }

    public GatherIsIpv6SNMPRunnable(NetworkElement networkElement, Date date) {
        this.networkElement = networkElement;
        this.date = date;
    }

    public GatherIsIpv6SNMPRunnable(NetworkElement networkElement, Date date, CountDownLatch latch) {
        this.networkElement = networkElement;
        this.date = date;
        this.latch = latch;
    }

    @Override
    public void run() {
        try {
            INetworkElementService networkElementServiceImpl = (INetworkElementService) ApplicationContextUtils.getBean("networkElementServiceImpl");
//            SNMPParams snmpParams = new SNMPParams(networkElement.getIp(), networkElement.getVersion(), networkElement.getCommunity());
//            Boolean result = SNMPv2Request.getIsV6(snmpParams);
            Boolean result = SNMPv3Request.getIsV6(SNMPParamFactory.createSNMPParam(networkElement));

            if (result != null) {
                networkElement.setIsipv6(result);
                networkElementServiceImpl.update(networkElement);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (latch != null) {
                latch.countDown();
            }
        }
    }
}
