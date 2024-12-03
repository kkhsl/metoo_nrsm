package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnboundDTO;
import com.metoo.nrsm.core.service.IUnboundService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unbound;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;

@RequestMapping("/admin/unbound")
@RestController
public class UnboundManagerController {

    @Autowired
    private IUnboundService unboundService;

    @PostMapping("/save")
    private Result add(@RequestBody UnboundDTO instance){
        boolean flag = this.unboundService.add(instance);
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }


    @DeleteMapping("/delete")
    private Result unbound(@RequestParam String id){
        boolean flag = this.unboundService.delete(Long.parseLong(id));
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("重复删除");
    }


    @GetMapping("/select")
    private Result unbound(){
        Unbound unbound = this.unboundService.selectObjByOne(Collections.emptyMap());
        return ResponseUtil.ok(unbound);
    }


    @PostMapping("/saveDNS")
    private Result DNS(@RequestBody UnboundDTO instance){
        boolean flag = this.unboundService.addDNS(instance);
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping("/resetDNS")
    private Result resetDNS(@RequestParam String id){
        boolean flag = this.unboundService.deleteDNS(Long.parseLong(id));
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error("重复删除");
    }






    @DeleteMapping("/deleteAll")
    private Result delete(@RequestParam String id){
        boolean flag = this.unboundService.delete(Long.parseLong(id));
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @PostMapping("/savaAll")
    private Result unbound(@RequestBody UnboundDTO instance){
        boolean flag = this.unboundService.update(instance);
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }





}
