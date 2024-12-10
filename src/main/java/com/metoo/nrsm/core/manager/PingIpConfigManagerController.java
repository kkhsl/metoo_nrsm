package com.metoo.nrsm.core.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.service.IPingIpConfigService;
import com.metoo.nrsm.core.service.IPingService;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.core.service.impl.UnboundServiceImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.wsapi.utils.Md5Crypt;
import com.metoo.nrsm.entity.Ping;
import com.metoo.nrsm.entity.PingIpConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.concurrent.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-18 16:41
 */
@RequestMapping("/admin/ping/ip/config")
@RestController
public class PingIpConfigManagerController {

    @Autowired
    private IPingIpConfigService pingIpConfigService;
    @Autowired
    private IPingService pingService;
    @Resource
    private UnboundServiceImpl unboundService;

    private final CopyOnWriteArrayList<Ping> pingResults = new CopyOnWriteArrayList<>();
    private final CopyOnWriteArrayList<PingIpConfig> pingIpConfigs = new CopyOnWriteArrayList<>();

    @GetMapping("status")
    public Result status(){
        boolean f = this.pingIpConfigService.status();
        return ResponseUtil.ok(f);
    }


    @GetMapping
    public Result ipConfig(){
        PingIpConfig pingIpConfig = this.pingIpConfigService.selectOneObj();
        return ResponseUtil.ok(pingIpConfig);
    }

    @PutMapping
    public Result update(@RequestBody PingIpConfig instance){
        //PingIpConfig oldPingIpConfig = this.pingIpConfigService.selectOneObj();
        boolean flag = this.pingIpConfigService.update(instance);
        if(flag){
            Integer status = instance.getStatus();
            boolean bool = status != 0;
            // 查询checkaliveip状态
            boolean checkaliveStatus = this.pingIpConfigService.status();
            if(bool){
                if(!checkaliveStatus){
                    try {
                        this.pingIpConfigService.start();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
                if(checkaliveStatus){
                    this.pingIpConfigService.restart();
                    this.pingIpConfigService.checkaliveip();
                    // 是否判断用户是否修改内容？如果未修改，也根据用户刷新页面,检查链路是否可达
                    // 异步执行链路检测
                    CompletableFuture.runAsync(() -> {
                        for (int i = 0; i < 3; i++) {
                            Ping ping1 = this.pingService.selectOneObj();
                            PingIpConfig pingIpConfig2 = this.pingIpConfigService.selectOneObj();
                            pingResults.add(ping1);
                            pingIpConfigs.add(pingIpConfig2); // 存储当前配置
                /*boolean checkaliveip = "1".equals(ping.getV6isok()); // 链路通，注释
                if(!checkaliveip){
                    unboundDTO.setPrivateAddress(true);// 链路不通，去掉注释：true
                    unboundService.open(unboundDTO);
                }else{
                    unboundDTO.setPrivateAddress(false);
                    unboundService.open(unboundDTO);
                }*/
                            try {
                                Thread.sleep(60 * 1000); // 每次查询之间间隔1分钟
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        // 启动定时任务
                        startScheduledTask();
                    });
                }else{
                    // 程序未启动如何提示？
                }
            }else{
                if(checkaliveStatus){
                    try {
                        this.pingIpConfigService.stop();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @GetMapping("/checkaliveip")
    public Result checkaliveip(){
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
        boolean bool = pingIpConfig.getStatus() != 0;
        if(!bool){// 不启用，注释
            unboundDTO.setPrivateAddress(false);
            unboundService.open(unboundDTO);
            boolean restart = unboundService.restart();
            return ResponseUtil.ok(ping);
        }
        return ResponseUtil.ok(ping);
    }

    // 定义一个定时任务，三分钟后执行；参数，ip地址，开关，如果任意数据改变，则重启开启一个三分钟的任务？
    private void startScheduledTask() {
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
        Runnable task = () -> {
            // 检查前三次的结果是否一致
            boolean allEqual = pingResults.stream().allMatch(result -> result.equals(pingResults.get(0)));
            boolean configEqual = pingIpConfigs.stream().allMatch(config -> config.equals(pingIpConfigs.get(0)));
            if (!allEqual || !configEqual) {
                pingResults.clear();
                pingIpConfigs.clear();
                System.out.println(0);
                // 结果不一致
                return;
            }
            // 如果结果一致，检查链路状态并执行修改
            Ping lastPingResult = pingResults.get(0);
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
            pingResults.clear();
            pingIpConfigs.clear();
        };
        scheduler.schedule(task, 1, TimeUnit.SECONDS);
    }


}
