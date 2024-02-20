package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.service.IGatherService;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.entity.nspm.Ipv4;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-02-01 15:14
 */
@RequestMapping("/admin/gather")
@RestController
public class GatherManagerController {

    @Autowired
    private IGatherService gatherService;

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


    @Autowired
    private Ipv4Service ipv4Service;

    // 测试事务，tuncate后，是否导致脏读
    @GetMapping("testTruncate")
    public void testTruncate() throws InterruptedException {

//        this.ipv4Service.deleteTable();

        this.ipv4Service.copyGatherToIpv4();


    }

}
