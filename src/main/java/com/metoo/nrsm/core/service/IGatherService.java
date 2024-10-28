package com.metoo.nrsm.core.service;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 10:29
 */
public interface IGatherService {

    void gatherMac(Date date);

    void gatherMacThread(Date date);

    void gatherArp(Date date);

    void gatherIpv4(Date date);

    void gatherIpv4Thread(Date date);

    void gatherIpv4Detail(Date date);

    void gatherIpv6(Date date);

    void gatherIpv6Thread(Date date);

    void gatherPort(Date date);

    void gatherPortIpv6(Date date);

    void gatherIsIpv6(Date date);

    void gatherFlux(Date date);

    void exec(Date date);

    void pingSubnet();

    void gatherSnmpStatus();

}
