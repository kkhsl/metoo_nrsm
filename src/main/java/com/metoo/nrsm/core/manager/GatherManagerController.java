package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.mapper.TerminalMapper;
import com.metoo.nrsm.core.service.IDhcp6Service;
import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.core.service.IGatherService;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.PythonExecUtils;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.gather.gathermac.GatherMacUtils;
import com.metoo.nrsm.entity.Ipv6;
import com.metoo.nrsm.entity.Terminal;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:14
 */
@Slf4j
@RequestMapping("/admin/gather")
@RestController
public class GatherManagerController {

    @Autowired
    private IGatherService gatherService;
    @Autowired
    private GatherMacUtils gatherMacUtils;

    @GetMapping("arp")
    public void gatherArp(){
        this.gatherService.gatherArp(new Date());
    }

    @GetMapping("ipv4")
    public void gatherIpv4(){
       this.gatherService.gatherIpv4(new Date());
    }

    @GetMapping("ipv4/thread")
    public void gatherIpv4Thread(){
        this.gatherService.gatherIpv4Thread(new Date());
    }

    @GetMapping("ipv6")
    public void gatherIpv6(){
        this.gatherService.gatherIpv6(new Date());
    }

    @GetMapping("ipv6/thread")
    public void gatherIpv6Thread(){
        this.gatherService.gatherIpv6Thread(new Date());
    }

    @GetMapping("mac")
    public void gatherMac(){
        this.gatherService.gatherMac(new Date());
    }

    @GetMapping("mac/thread")
    public void gatherMacThread(){
        this.gatherService.gatherMacThread(new Date());
    }


    @GetMapping("mac/mac_dt")
    public void mac_dt(){
        this.gatherMacUtils.mac_dt();
    }

    @GetMapping("mac/tag_x")
    public void tag_x(){
        this.gatherMacUtils.tag_x();
    }

    @GetMapping("mac/tag_u")
    public void tag_u(){
        this.gatherMacUtils.tag_u();
    }

    @GetMapping("mac/tag_s")
    public void tag_s(){
        this.gatherMacUtils.tag_s();
    }

    @GetMapping("mac/tag_STOE")
    public void tag_STOE(){
        this.gatherMacUtils.tag_STOE();
    }


    @GetMapping("mac/copyArpMacAndIpToMac")
    public void copyArpMacAndIpToMac(){
        this.gatherMacUtils.copyArpMacAndIpToMac();
    }

    @GetMapping("mac/tag_XToE")
    public void tag_XToE(){
        this.gatherMacUtils.tag_XToE();
    }
    @GetMapping("mac/tag_UToE")
    public void tag_UToE(){
        this.gatherMacUtils.tag_UToE();
    }

    @GetMapping("mac/selectUToRT")
    public void selectUToUTByMap(){
        this.gatherMacUtils.selectUToRT();
    }

    @GetMapping("mac/RTToDT")
    public void RTToDT(){
        this.gatherMacUtils.RTToDT();
    }

    @GetMapping("mac/RTToDT2")
    public void RTToDT2(){
        this.gatherMacUtils.RTToDT();
    }

    @GetMapping("mac/copyArpIpToMacByDT")
    public void copyArpIpToMacByDT(){
        this.gatherMacUtils.copyArpIpToMacByDT();
    }


    @Autowired
    private Ipv4Service ipv4Service;

    // 测试事务，tuncate后，是否导致脏读
    @GetMapping("testTruncate")
    public void testTruncate() throws InterruptedException {

//        this.ipv4Service.deleteTable();

//        this.ipv4Service.copyGatherToIpv4();

        this.gatherMacUtils.testTransaction();

    }

    @Autowired
    private IDhcpService dhcpService;
    @Autowired
    private IDhcp6Service dhcp6Service;

    @GetMapping("terminal")
    public void terminal() {
        List<Terminal> terminals = this.terminalMapper.selectObjByMap(null);
        for (Terminal terminal : terminals) {
            terminal.setOnline(true);
            terminal.setDeviceTypeUuid("1");
        }
        this.terminalMapper.batchUpdate(terminals);
    }


    @GetMapping("dhcp")
    public void gatherDhcp() {
        dhcpService.gather(DateTools.gatherDate());
    }

    @GetMapping("dhcp6")
    public void dhcp6() {
        dhcp6Service.gather(DateTools.gatherDate());
    }

    @GetMapping("port")
    public void port() {
        gatherService.gatherPort(DateTools.gatherDate());
    }

    @GetMapping("portIpv6")
    public void portIpv6() {
        gatherService.gatherPortIpv6(DateTools.gatherDate());
    }




    @GetMapping("getarpv6JSON")
    public void getarpv6JSON(String ip) {
        String path = Global.PYPATH +  "getarpv6.py";
        String[] params = {ip, "v2c",
                "public@123"};
        String result = PythonExecUtils.exec(path, params);
        if(StringUtil.isNotEmpty(result)) {
            try {
                List<Ipv6> array = JSONObject.parseArray(result, Ipv6.class);
            } catch (Exception e) {
                e.printStackTrace();
                log.info(ip + " : " + result);
            }

        }}


    @Autowired
    private TerminalMapper terminalMapper;

    @GetMapping("selectObjLeftdifference")
    public Object selectObjLeftdifference() {
        List<Terminal> left = this.terminalMapper.selectObjLeftdifference();
        return left;
    }




}
