package com.metoo.nrsm.core.manager.scan;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.body.ProbeBody;
import com.metoo.nrsm.core.service.IArpService;
import com.metoo.nrsm.core.service.IMacVendorService;
import com.metoo.nrsm.core.service.IProbeResultService;
import com.metoo.nrsm.core.service.IProbeService;
import com.metoo.nrsm.core.utils.net.Ipv6Utils;
import com.metoo.nrsm.entity.Arp;
import com.metoo.nrsm.entity.Probe;
import com.metoo.nrsm.entity.ProbeResult;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-06-25 22:29
 */
@Slf4j
@RequestMapping
@RestController
public class ProbeManagerController {

    @Autowired
    private IProbeService probeService;
    @Autowired
    private IProbeResultService probeResultService;
    @Autowired
    private IArpService arpService;
    @Autowired
    private IMacVendorService macVendorService;

    public static void main(String[] args) {
        String result = "[{\"createTime\":1733036653000,\"fingerIdOsScan\":\"IBM AIX 5.3\",\"id\":14,\"ip_addr\":\"192.168.5.51\",\"mac\":\"00:25:9e:03:76:12\",\"os_family\":\"5.3\",\"os_gen\":\"aix\",\"port_num\":\"2\",\"reliability\":0.9,\"ttl\":255,\"vendor\":\"ibm\"},{\"createTime\":1733036653000,\"id\":15,\"ip_addr\":\"192.168.5.55\",\"mac\":\"58:48:49:2f:f8:4a\",\"port_num\":\"2\"},{\"createTime\":1733036653000,\"id\":16,\"ip_addr\":\"192.168.5.101\",\"mac\":\"a0:36:9f:17:e4:c6\",\"port_num\":\"2\"},{\"createTime\":1733036653000,\"id\":17,\"ip_addr\":\"192.168.5.205\",\"mac\":\"58:48:49:27:54:fb\",\"port_num\":\"2\"},{\"createTime\":1733036653000,\"fingerIdOsScan\":\"Adtran Total Access 904 router\",\"id\":18,\"ip_addr\":\"192.168.6.65\",\"mac\":\"00:25:b3:c8:69:e8\",\"os_gen\":\"total_access_904\",\"port_num\":\"2\",\"reliability\":0.73,\"vendor\":\"adtran\"},{\"createTime\":1733036653000,\"fingerIdOsScan\":\"Adtran Total Access 904 router\",\"id\":19,\"ip_addr\":\"192.168.6.67\",\"mac\":\"00:e0:4c:69:66:36\",\"os_gen\":\"total_access_904\",\"port_num\":\"2\",\"reliability\":0.73,\"vendor\":\"adtran\"},{\"createTime\":1733036654000,\"fingerIdOsScan\":\"Adtran Total Access 904 router\",\"id\":20,\"ip_addr\":\"192.168.6.76\",\"mac\":\"30:b4:9e:33:51:8d\",\"os_gen\":\"total_access_904\",\"port_num\":\"2\",\"reliability\":0.73,\"vendor\":\"adtran\"},{\"createTime\":1733036654000,\"fingerIdOsScan\":\"Linux 2.4.21\",\"id\":21,\"ip_addr\":\"192.168.6.77\",\"mac\":\"50:33:f0:a4:33:c4\",\"os_family\":\"2.4.21\",\"os_gen\":\"linux_kernel\",\"port_num\":\"2\",\"reliability\":0.96,\"ttl\":63,\"vendor\":\"linux\"},{\"createTime\":1733036654000,\"fingerIdOsScan\":\"Adtran Total Access 904 router\",\"id\":22,\"ip_addr\":\"192.168.6.88\",\"mac\":\"00:e0:4c:69:66:52\",\"os_gen\":\"total_access_904\",\"port_num\":\"2\",\"reliability\":0.73,\"vendor\":\"adtran\"},{\"createTime\":1733036654000,\"fingerIdOsScan\":\"Adtran Total Access 904 router\",\"id\":23,\"ip_addr\":\"192.168.6.97\",\"mac\":\"32:fa:3b:68:59:21\",\"os_gen\":\"total_access_904\",\"port_num\":\"2\",\"reliability\":0.73,\"vendor\":\"adtran\"},{\"createTime\":1733036654000,\"fingerIdOsScan\":\"Adtran Total Access 904 router\",\"id\":24,\"ip_addr\":\"192.168.6.252\",\"mac\":\"58:41:20:86:00:c0\",\"os_gen\":\"total_access_904\",\"port_num\":\"2\",\"reliability\":0.73,\"vendor\":\"adtran\"},{\"createTime\":1733036654000,\"fingerIdOsScan\":\"Adtran Total Access 904 router\",\"id\":25,\"ip_addr\":\"192.168.6.253\",\"mac\":\"74:05:a5:2c:38:0d\",\"os_gen\":\"total_access_904\",\"port_num\":\"2\",\"reliability\":0.73,\"vendor\":\"adtran\"}]";
        List<Probe> probeDataList = JSONObject.parseArray(result, Probe.class);
    }

    @PostMapping("/probeNmap/uploadScanResult")
    public String probe(@RequestBody ProbeBody body) {
        Map result = new HashMap();
        result.put("code", 0);
        result.put("message", null);
        result.put("taskuuid", body.getTaskuuid());
        if (body.getTaskuuid() == null || "".equals(body.getTaskuuid())) {
            result.put("code", 1);
        }
        if (body.getResult() == null || body.getResult().equals("")) {
            result.put("code", 1);
        } else {
            log.info("chuangfa---------------------------：" + body.getResult());
            List<Probe> probeDataList = JSONObject.parseArray(body.getResult(), Probe.class);
            log.info("chuangfa result size ---------------------------：" + probeDataList.size());
            if (probeDataList.size() > 0) {
                for (Probe probe : probeDataList) {
                    if(Ipv6Utils.isValidIPv6(probe.getIp_addr())){
                        probe.setIpv6(probe.getIp_addr());
                        probe.setIp_addr(null);
                    }
//                    // 根据ip地址查询arp表数据获取mac、ipv6等信息
                    if(StrUtil.isNotEmpty(probe.getIp_addr()) || StrUtil.isNotEmpty(probe.getIpv6())) {
                        List<Arp> arpList = arpService.selectObjByMap(MapUtil.of("v4ip", probe.getIp_addr()));
                        if(CollUtil.isNotEmpty(arpList)){
                            probe.setIp_addr(arpList.get(0).getV4ip());
                            probe.setIpv6(arpList.get(0).getV6ip());
                            probe.setMac(arpList.get(0).getMac());
                            probe.setMac_vendor(arpList.get(0).getMacVendor());
                        }
                    }
                    boolean flag = this.probeService.insert(probe);
                    System.out.println("probe 插入状态：" + flag);

                }
            }
        }

        ProbeResult probeResult = this.probeResultService.selectObjByOne();
        probeResult.setResult(probeResult.getResult() + 1);
        this.probeResultService.update(probeResult);

        return JSONObject.toJSONString(result);

    }


}
