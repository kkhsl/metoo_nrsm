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
import com.metoo.nrsm.entity.scan.Probe;
import com.metoo.nrsm.entity.scan.ProbeResult;
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
                        List<Arp> arpList = arpService.selectObjByMap(MapUtil.of("ipAddress", probe.getIp_addr()));
                        if(CollUtil.isNotEmpty(arpList)){
                            probe.setIp_addr(arpList.get(0).getV4ip());
                            probe.setIpv6(arpList.get(0).getV6ip());
                            probe.setMac(arpList.get(0).getMac());
                            probe.setMac_vendor(arpList.get(0).getMacVendor());
                        }
                    }
                    this.probeService.insert(probe);
                }
            }
        }



        ProbeResult probeResult = this.probeResultService.selectObjByOne();
        probeResult.setResult(probeResult.getResult() + 1);
        this.probeResultService.update(probeResult);

        return JSONObject.toJSONString(result);

    }


}
