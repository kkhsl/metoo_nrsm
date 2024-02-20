package com.metoo.nrsm.core.service;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-20 10:29
 */
public interface IGatherService {

    public void gatherMac(Date date);

    public void gatherArp(Date date);

    public void gatherIpv4(Date date);

    public void gatherIpv4Thread(Date date);

    public void gatherIpv6(Date date);

    public void gatherIpv6Thread(Date date);

}
