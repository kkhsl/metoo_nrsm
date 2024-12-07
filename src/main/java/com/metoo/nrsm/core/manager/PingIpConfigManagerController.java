package com.metoo.nrsm.core.manager;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.service.IPingIpConfigService;
import com.metoo.nrsm.core.service.IPingService;
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


    @GetMapping
    public Result ipConfig(){
        PingIpConfig pingIpConfig = this.pingIpConfigService.selectOneObj();
        return ResponseUtil.ok(pingIpConfig);
    }

    @PutMapping
    public Result update(@RequestBody PingIpConfig instance){
        PingIpConfig oldPingIpConfig = this.pingIpConfigService.selectOneObj();
        boolean flag = this.pingIpConfigService.update(instance);
        UnboundDTO unboundDTO = new UnboundDTO();
        if(flag){
            try {
                Integer status = instance.getStatus();

                //boolean checkaliveip = this.pingIpConfigService.checkaliveip();
                Ping ping = this.pingService.selectOneObj();
                boolean checkaliveip = "1".equals(ping.getV6isok());

                boolean bool = status != 0;

                oldPingIpConfig.setStatus(0);

                instance.setStatus(0);

                boolean diffrent = Md5Crypt.getDiffrent(oldPingIpConfig, instance);
                if(bool && !checkaliveip /*&& diffrent*/){
                    boolean start = this.pingIpConfigService.start();
                    if(!start){
                        unboundDTO.setPrivateAddress(true);
                        unboundService.open(unboundDTO);
                        return ResponseUtil.ok("进程启动失败");
                    }
                }

                if(bool && checkaliveip /*&& diffrent*/){
                    boolean start = this.pingIpConfigService.start();
                    if(!start){
                        unboundDTO.setPrivateAddress(false);
                        unboundService.open(unboundDTO);
                        return ResponseUtil.ok("进程启动成功");
                    }
                }

                if(!bool && checkaliveip){
                    boolean stop = this.pingIpConfigService.stop();
                    if(!stop){
                        unboundDTO.setPrivateAddress(false);
                        unboundService.open(unboundDTO);
                        return ResponseUtil.ok("进程关闭失败");
                    }
                }
                if(!bool && !checkaliveip){
                    unboundDTO.setPrivateAddress(true);
                    unboundService.open(unboundDTO);
                    return ResponseUtil.ok("进程关闭成功");
                }

                if(bool && !diffrent){
                    boolean restart = this.pingIpConfigService.restart();
                    if(!restart){
                        unboundDTO.setPrivateAddress(true);
                        unboundService.open(unboundDTO);
                        return ResponseUtil.ok("进程重启失败");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            unboundDTO.setPrivateAddress(false);
            unboundService.open(unboundDTO);
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
        Ping ping = this.pingService.selectOneObj();
        return ResponseUtil.ok(ping);
    }
}
