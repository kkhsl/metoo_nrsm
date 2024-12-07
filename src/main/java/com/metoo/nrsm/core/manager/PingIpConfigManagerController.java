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

    @Autowired
    private IUnboundService iUnboundService;



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

                boolean checkaliveip = this.pingIpConfigService.checkaliveip();
                boolean bool = status != 0;

                oldPingIpConfig.setStatus(0);

                instance.setStatus(0);

                boolean diffrent = Md5Crypt.getDiffrent(oldPingIpConfig, instance);
                if(bool && !checkaliveip /*&& diffrent*/){
                    boolean start = this.pingIpConfigService.start();
                    if(!start){
                        return ResponseUtil.ok("进程启动失败");
                    }
                }

                if(bool && checkaliveip /*&& diffrent*/){
                    boolean start = this.pingIpConfigService.start();
                    if(!start){
                        return ResponseUtil.ok("进程启动成功");
                    }
                }

                if(!bool && checkaliveip){
                    boolean stop = this.pingIpConfigService.stop();
                    if(!stop){
                        return ResponseUtil.ok("进程关闭失败");
                    }
                }
                if(!bool && !checkaliveip){
                    return ResponseUtil.ok("进程关闭成功");
                }

                if(bool && !diffrent){
                    boolean restart = this.pingIpConfigService.restart();
                    if(!restart){
                        return ResponseUtil.ok("进程重启失败");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }finally {
                try {

                    boolean restart = iUnboundService.restart();
                    if (restart){
                        return ResponseUtil.ok("重启成功");
                    }else {
                        return ResponseUtil.ok("重启失败");
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
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
        // 是否判断用户是否修改内容？如果未修改，也根据用户刷新页面,检查链路是否可达
        Ping ping = this.pingService.selectOneObj();
        boolean checkaliveip = "1".equals(ping.getV6isok());// 链路通，注释
        UnboundDTO unboundDTO = new UnboundDTO();
        if(!checkaliveip){
            unboundDTO.setPrivateAddress(true);// 链路不通，去掉注释：true
            unboundService.open(unboundDTO);
        }else{
            unboundDTO.setPrivateAddress(false);
            unboundService.open(unboundDTO);
        }
        return ResponseUtil.ok(ping);
    }
}
