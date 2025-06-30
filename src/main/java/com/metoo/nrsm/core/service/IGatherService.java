package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.NetworkElement;

import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 10:29
 */
public interface IGatherService {


    Map gatherMac(Date date, List<NetworkElement> networkElements);

    void gatherMacThread(Date date);

    void gatherArp(Date date);

    void gatherIpv4(Date date);

    Map gatherIpv4Thread(Date date, List<NetworkElement> networkElements);

    void gatherIpv4Detail(Date date);

    Map gatherIpv6(Date date, List<NetworkElement> networkElements);

    Map gatherIpv6Thread(Date date, List<NetworkElement> networkElements);

    void gatherPort(Date date, List<NetworkElement> networkElements);

    void gatherPortIpv6(Date date, List<NetworkElement> networkElements);

    void gatherIsIpv6(Date date);

    void gatherFlux(Date date);

    void exec(Date date);

    void pingSubnet();

    void gatherSnmpStatus();

}
