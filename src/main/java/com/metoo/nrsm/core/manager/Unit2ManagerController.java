package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IUnit2Service;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit2;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RequestMapping("/admin/unit")
@RestController
public class Unit2ManagerController {

    @Autowired
    private IUnit2Service unit2Service;

    @PostMapping("/list")
    private Result list(@RequestBody Unit2 dto){
        Result result = this.unit2Service.selectObjConditionQuery(dto);
        return result;
    }

    @GetMapping("/selectAll")
    private Result selectAll(){
        Result result = this.unit2Service.selectAllQuery();
        return result;
    }

    @PostMapping("/save")
    private Result save(@RequestBody Unit2 instance){
        Result result = this.unit2Service.save(instance);
        return result;
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam String ids) {
        Unit2 unit2 = unit2Service.selectObjById(Long.valueOf(ids));
        if (unit2 != null) {
            unit2.setDeleteStatus(1);
            unit2Service.update(unit2);
            return ResponseUtil.ok();
        }
        return ResponseUtil.badArgument();


    }

}
