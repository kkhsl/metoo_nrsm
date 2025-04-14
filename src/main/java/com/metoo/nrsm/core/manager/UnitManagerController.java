package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitDTO;
import com.metoo.nrsm.core.service.IUnit2Service;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.utils.string.StringUtils;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Slf4j
@RequestMapping("/admin/flow/unit")
@RestController
public class UnitManagerController {

    @Autowired
    private IUnitService unitService;

    @PostMapping("/list")
    private Result list(@RequestBody UnitDTO dto){
        Result result = this.unitService.selectObjConditionQuery(dto);
        return result;
    }

    @GetMapping("/selectAll")
    private Result selectAll(){
        Result result = this.unitService.selectAllQuery();
        return result;
    }


    @GetMapping("/add")
    private Result add(){
        Result result = this.unitService.add();
        return result;
    }

    @PostMapping("/save")
    private Result save(@RequestBody Unit instance){
        Result result = this.unitService.save(instance);
        return result;
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam String ids) {
        Result result = this.unitService.delete(ids);
        return result;
    }

    @PutMapping("/modify")
    public Result modify(@RequestParam String id) {
        Unit unit = this.unitService.selectObjById(Long.parseLong(id));
        if(unit != null){
            unit.setHidden(!unit.isHidden());
            this.unitService.update(unit);
        }
        return ResponseUtil.ok();
    }
}
