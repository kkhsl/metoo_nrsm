package com.metoo.nrsm.core.manager.scan;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.druid.sql.visitor.functions.Char;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.body.ProbeBody;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.net.Ipv6Utils;
import com.metoo.nrsm.entity.Arp;
import com.metoo.nrsm.entity.Probe;
import com.metoo.nrsm.entity.ProbeResult;
import com.metoo.nrsm.entity.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomStringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

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
    private ITerminalService terminalService;

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
                Map params = new HashMap();
                for (Probe probe : probeDataList) {
                    if (Ipv6Utils.isValidIPv6(probe.getIp_addr())) {
                        probe.setIpv6(probe.getIp_addr());
                        probe.setIp_addr(null);
                    }
//                    // 根据ip地址查询arp表数据获取mac、ipv6等信息
                    if (StrUtil.isNotEmpty(probe.getIp_addr()) || StrUtil.isNotEmpty(probe.getIpv6())) {
                        List<Arp> arpList = arpService.selectObjByMap(MapUtil.of("v4ip", probe.getIp_addr()));
                        if (CollUtil.isNotEmpty(arpList)) {
                            probe.setIp_addr(arpList.get(0).getV4ip());
                            probe.setIpv6(arpList.get(0).getV6ip());
                            probe.setMac(arpList.get(0).getMac());
                            probe.setMac_vendor(arpList.get(0).getMacVendor());
                        }
                    }
                    List<Terminal> terminals = new ArrayList<>();
                    if (StrUtil.isNotEmpty(probe.getIp_addr())) {
                        params.clear();
                        params.put("v4ip", probe.getIp_addr());
                        terminals = this.terminalService.selectObjByMap(params);
                    }else if(StrUtil.isNotEmpty(probe.getIpv6())){
                        params.clear();
                        params.put("v6ip", probe.getIpv6());
                        terminals = this.terminalService.selectObjByMap(params);
                    }
                    if(terminals.size() > 0){
                        Terminal terminal = terminals.get(0);
                        probe.setUnitId(terminal.getUnitId());
                    }

                    // TODO 优化建议：改为批量插入，避免并发导致锁死
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


    // TODO 模拟并发回调
    @GetMapping("/addProbe")
    public void addProbe() throws InterruptedException {
        Probe probe = new Probe();
        probe.setMac("00:00:00:00:00:" + generateMacLastTwo());
        boolean f = this.probeService.insert(probe);
        log.info("插入状态：{}", f);
    }

    public static void main(String[] args) {
        String firstString = RandomStringUtils.randomAlphanumeric(2); // 5个字母数字字符
        String secondString = RandomStringUtils.randomAlphabetic(2); // 5个字母字符

        System.out.println("第一个随机字符串: " + firstString);
        System.out.println("第二个随机字符串: " + secondString);

        String lastTwo = generateMacLastTwo();
        System.out.println("MAC地址最后两位: " + lastTwo);

        System.out.println(new Random().nextInt());
    }

    public static String generateMacLastTwo() {
        Random random = new Random();

        // 方式一：
        char[] hexChars = "0123456789ABCDEF".toCharArray();

        // 生成第一个十六进制字符
        char first = hexChars[random.nextInt(16)];
        // 生成第二个十六进制字符
        char second = hexChars[random.nextInt(16)];

        return String.valueOf(first) + String.valueOf(second);

        // 方式二
//        Random random = new Random();
        // 生成0-255的随机数，然后格式化为两位十六进制
//        int value = random.nextInt(256);
//        return String.format("%02X", value);
    }
}
