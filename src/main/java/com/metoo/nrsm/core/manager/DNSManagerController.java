package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IDNSService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.service.impl.UserServiceImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Dns;
import com.metoo.nrsm.entity.User;
import org.springframework.aop.framework.AopContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/admin/dns")
public class DNSManagerController {

    @Autowired
    private IDNSService dnsService;

    @GetMapping("all")
    public Result all(){
        List<Dns> result = this.dnsService.selectObjByMap(null);
        return ResponseUtil.ok(result);
    }

    @PostMapping("save")
    public Result save(@RequestBody Dns instance){
        boolean flag = this.dnsService.save(instance);
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping("delete")
    public Object delete(String ids){
        if(ids != null && !ids.equals("")){
            for (String id : ids.split(",")){
                Map params = new HashMap();
                params.put("id", Long.parseLong(id));
                List<Dns> dns = this.dnsService.selectObjByMap(params);
                if(dns.size() > 0){
                    Dns ne = dns.get(0);
                    try {
                        boolean i = this.dnsService.delete(Long.parseLong(id));
                    } catch (NumberFormatException e) {
                        e.printStackTrace();
                        return ResponseUtil.badArgument(ne.getQname() + "删除失败");
                    }
                }else{
                    return ResponseUtil.badArgument();
                }
            }
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();
    }

    public static void main(String[] args) {
        String old = "aaa.apple.tree";
        String ne = "apples.tree";
        String newDomain = old.substring(0, old.indexOf("."));
        String a = newDomain + "." + ne;
        System.out.println(a);
    }


    @PutMapping("modify/primary/domain")
    public Result modify(String oldDomain, String newDomain){
        List<Dns> dnsList = this.dnsService.selectObjByPrimaryDomain(oldDomain);
        if(dnsList.size() > 0){
            for (Dns dns : dnsList) {
                dns.setQname(dns.getQname().substring(0, dns.getQname().indexOf("."))+ "." + newDomain);
                this.dnsService.update(dns);
            }
        }
        return ResponseUtil.ok();
    }

    @GetMapping("getdns")
    public Result internet(){
        String result = this.dnsService.get();
        return ResponseUtil.ok(result);
    }

    @PostMapping("modifydns")
    public Result modifydns(@RequestBody String[] params){
        String result = this.dnsService.modifydns(params);
        if(result.equals("None")){
            return ResponseUtil.ok();
        }else{
            return ResponseUtil.error("保存失败");
        }
    }


}
