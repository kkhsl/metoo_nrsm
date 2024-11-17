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

    @GetMapping
    private Result unbound(){
        Unbound unbound = this.unboundService.selectObjByOne(Collections.emptyMap());
        return ResponseUtil.ok(unbound);
    }

    @PostMapping
    private Result unbound(@RequestBody UnboundDTO instance){
        boolean flag = this.unboundService.update(instance);
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping
    private Result unbound(@RequestParam String id){
        boolean flag = this.unboundService.delete(Long.parseLong(id));
        if(flag){
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

}
