package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.GatewayDTO;
import com.metoo.nrsm.core.service.IGatewayService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Gateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RequestMapping("/admin/gateway")
@RestController
public class GatewayManagerController {

    @Autowired
    private IGatewayService gatewayService;

    @PostMapping("/list")
    private Result list(@RequestBody GatewayDTO dto){
        Result result = this.gatewayService.selectObjConditionQuery(dto);
        return result;
    }

    @PostMapping("/save")
    private Result save(@RequestBody Gateway instance){
        Result result = this.gatewayService.save(instance);
        return result;
    }

    @PostMapping("/batch/save")
    private Result batchSave(@RequestBody List<Gateway> devices){
        if(devices.size() > 0){
            Result result = this.gatewayService.batchSave(devices);
            return result;
        }
        return ResponseUtil.ok();
    }

    @GetMapping("/modify")
    public Result modify(@RequestParam Long id) {
        Result result = this.gatewayService.modify(id);
        return ResponseUtil.ok(result);
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam String ids) {
        Result result = this.gatewayService.delete(ids);
        return result;
    }

    @GetMapping("/test")
    public Result test(@RequestParam String ids) {
        Result result = this.gatewayService.delete(ids);
        return result;
    }
}
