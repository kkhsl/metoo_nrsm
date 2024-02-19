package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IDNSService;
import com.metoo.nrsm.core.vo.Result;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-29 16:07
 */
@RestController
@RequestMapping("/admin/dns")
public class DNSManagerController {

    @Autowired
    private IDNSService dnsService;

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

//    @RequestMapping("modifydns")
//    public Result modifydns(String[] params){
//        String result = this.dnsService.modifydns(params);
//        return ResponseUtil.ok(result);
//    }

}
