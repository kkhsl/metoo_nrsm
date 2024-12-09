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
        PingIpConfig oldPingIpConfig = this.pingIpConfigService.selectOneObj();
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
    public Result ipv6isok(){

        try {
            Thread.sleep(100);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        UnboundDTO unboundDTO = new UnboundDTO();

        PingIpConfig pingIpConfig = this.pingIpConfigService.selectOneObj();
        boolean bool = pingIpConfig.getStatus() != 0;
        if(!bool){// 不启用，注释
            unboundDTO.setPrivateAddress(false);
            unboundService.open(unboundDTO);
            return ResponseUtil.ok();
        }
        // 是否判断用户是否修改内容？如果未修改，也根据用户刷新页面,检查链路是否可达
        Ping ping = this.pingService.selectOneObj();
        boolean checkaliveip = "1".equals(ping.getV6isok());// 链路通，注释

        if(!checkaliveip){
            unboundDTO.setPrivateAddress(true);// 链路不通，去掉注释：true
            unboundService.open(unboundDTO);
        }else{
            unboundDTO.setPrivateAddress(false);
            unboundService.open(unboundDTO);
        }
        return ResponseUtil.ok(ping);
    }

    // 定义一个定时任务，三分钟后执行；参数，ip地址，开关，如果任意数据改变，则重启开启一个三分钟的任务？
}
