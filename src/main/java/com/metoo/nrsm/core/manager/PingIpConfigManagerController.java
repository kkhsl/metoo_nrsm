package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.service.IPingIpConfigService;
import com.metoo.nrsm.core.service.IPingService;
import com.metoo.nrsm.core.service.impl.UnboundServiceImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Ping;
import com.metoo.nrsm.entity.PingIpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.concurrent.*;

@RequestMapping("/admin/ping/ip/config")
@RestController
public class PingIpConfigManagerController {

    @Autowired
    private IPingIpConfigService pingIpConfigService;
    @Autowired
    private IPingService pingService;
    @Resource
    private UnboundServiceImpl unboundService;

    @GetMapping("status")
    public Result status() {
        boolean f = this.pingIpConfigService.status();
        return ResponseUtil.ok(f);
    }


    @GetMapping
    public Result ipConfig() {
        PingIpConfig pingIpConfig = this.pingIpConfigService.selectOneObj();
        return ResponseUtil.ok(pingIpConfig);
    }

    @PutMapping
    public Result update(@RequestBody PingIpConfig instance) {
        PingIpConfig oldPingIpConfig = this.pingIpConfigService.selectOneObj();
        boolean flag = this.pingIpConfigService.update(instance);
        if (flag) {
            Integer status = instance.getStatus();
            boolean bool = status != 0;
            // 查询checkaliveip状态
            boolean checkaliveStatus = oldPingIpConfig.isEnabled();
            //boolean checkaliveStatus = true;
            if (bool) {
                if (!checkaliveStatus) {
                    try {
                        instance.setEnabled(true);
                        this.pingIpConfigService.update(instance);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

//                    this.pingIpConfigService.restart();
//                    this.pingIpConfigService.checkaliveip();
                // 是否判断用户是否修改内容？如果未修改，也根据用户刷新页面,检查链路是否可达
                // 异步执行链路检测
                CompletableFuture.runAsync(() -> {
                    CopyOnWriteArrayList<Ping> pingResults = new CopyOnWriteArrayList<>();
                    CopyOnWriteArrayList<PingIpConfig> pingIpConfigs = new CopyOnWriteArrayList<>();
                    while (pingResults.size() < 4 || pingIpConfigs.size() < 3) {
                        try {
                            // 查询并存储记录
                            Ping ping1 = this.pingService.selectOneObj();
                            PingIpConfig pingIpConfig2 = this.pingIpConfigService.selectOneObj();

                            // 确保只保存不重复的数据
                            if (!pingResults.contains(ping1)) {
                                pingResults.add(ping1);
                            }
                            pingIpConfigs.add(pingIpConfig2);
                            // 查询之间间隔
                            Thread.sleep(10000);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                            Thread.currentThread().interrupt(); // 恢复中断状态
                        }
                    }
                    // 启动定时任务
                    startScheduledTask(pingResults, pingIpConfigs);
                });

            } else {
                instance.setEnabled(false);
                if (this.pingIpConfigService.update(instance)) {
                    return ResponseUtil.ok();
                }
                return ResponseUtil.error("链路自动检测关闭失败！");
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @GetMapping("/checkaliveip")
    public Result checkaliveip() {
        boolean flag = this.pingIpConfigService.checkaliveip();
        return ResponseUtil.ok(flag);
    }

    @GetMapping("/ipv6isok")
    public Result ipv6isok() throws Exception {
        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UnboundDTO unboundDTO = new UnboundDTO();
        PingIpConfig pingIpConfig = this.pingIpConfigService.selectOneObj();
        Ping ping = this.pingService.selectOneObj();
        if ((pingIpConfig == null || pingIpConfig.isEnabled()) == false) {
            Ping ping1 = new Ping();
            ping1.setIp1status("0");
            ping1.setIp2status("0");
            ping1.setIpv42status("0");
            ping1.setIpv41status("0");
            ping1.setV4isok("0");
            ping1.setV6isok("0");
            return ResponseUtil.ok(ping1);
        } else {
            boolean bool = pingIpConfig.getStatus() != 0;
            if (!bool) {// 不启用，注释
                unboundDTO.setPrivateAddress(false);
                unboundService.open(unboundDTO);
                boolean restart = unboundService.restart();
                return ResponseUtil.ok(ping);
            }
            return ResponseUtil.ok(ping);
        }
    }

    // 定义一个定时任务
    private void startScheduledTask(CopyOnWriteArrayList<Ping> pingResults, CopyOnWriteArrayList<PingIpConfig> pingIpConfigs) {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            List<Ping> lastThreePings = pingResults.subList(pingResults.size() - 3, pingResults.size());
            // 检查后连续三条结果是否一致
            boolean allEqual = lastThreePings.stream()
                    .map(Ping::getV6isok) // 提取 v6isok 属性
                    .allMatch(v6isok -> v6isok.equals(lastThreePings.get(0).getV6isok()));
            boolean configEqual = pingIpConfigs.stream().allMatch(config -> config.equals(pingIpConfigs.get(0)));
            if (!allEqual || !configEqual) {

                // 结果不一致
                System.out.println(0);
                return;
            }
            // 如果结果一致，检查链路状态并执行修改
            Ping lastPingResult = lastThreePings.get(0);
            UnboundDTO unboundDTO = new UnboundDTO();
            boolean checkaliveip = "1".equals(lastPingResult.getV6isok());
            if (!checkaliveip) {
                unboundDTO.setPrivateAddress(true); // 链路不通，去掉注释：true
                System.out.println(1);
            } else {
                unboundDTO.setPrivateAddress(false); // 链路通
                System.out.println(2);
            }
            unboundService.open(unboundDTO);
            try {
                boolean restart = unboundService.restart();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }

        };
        scheduler.schedule(task, 1, TimeUnit.SECONDS);
    }


}
