package com.metoo.nrsm.core.network.networkconfig.other.ipscanner;

import com.metoo.nrsm.core.network.concurrent.PingThreadPool;
import com.metoo.nrsm.core.network.networkconfig.other.ipscanner.scanners.CFScanner;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.ip.Ipv6Util;
import com.metoo.nrsm.entity.Ipv4;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;

/**
 * 使用创发cf-scanner扫默程序，扫描网段
 */
@Slf4j
@Component
public class PingCFScanner {

    public static void scan(String cidr) {
//        if (!Ipv4Util.verifyCidr(cidr)) {
//            log.warn("无效的CIDR格式: {}", cidr);
//            return;
//        }
        if (!Ipv4Util.verifyCidr(cidr) && !Ipv4Util.verifyIp(cidr)) {
            log.warn("无效的参数格式: {}", cidr);
            return;
        }
        PingThreadPool.execute(new CFScanner(cidr));
    }
}
