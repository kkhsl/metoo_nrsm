package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowUnit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 流量单位
 */
@Slf4j
@RequestMapping("/admin/flow/unit")
@RestController
public class FlowUnitManagerController {

    @Autowired
    private IFlowUnitService flowUnitService;

    @PostMapping("/list")
    private Result list(@RequestBody UnitNewDTO dto) {
        Result result = this.flowUnitService.selectObjConditionQuery(dto);
        return result;
    }

    @GetMapping("/selectAll")
    private Result selectAll() {
        Result result = this.flowUnitService.selectAllQuery();
        return result;
    }


    @GetMapping("/add")
    private Result add() {
        Result result = this.flowUnitService.add();
        return result;
    }

    @PostMapping("/save")
    private Result save(@RequestBody FlowUnit instance) {
        Result result = this.flowUnitService.save(instance);
        return result;
    }

    @DeleteMapping("/delete")
    public Result delete(@RequestParam String ids) {
        Result result = this.flowUnitService.delete(ids);
        return ResponseUtil.ok(result);
    }

    @PutMapping("/modify")
    public Result modify(@RequestParam String id) {
        FlowUnit unit = this.flowUnitService.selectObjById(Long.parseLong(id));
        if (unit != null) {
            unit.setHidden(!unit.isHidden());
            this.flowUnitService.update(unit);
        }
        return ResponseUtil.ok();
    }
}
