package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.mapper.FlowUnitMapper;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Unit;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 流量单位
 */
@Slf4j
@RequestMapping("/admin/flow/unit")
@RestController
public class FlowUnitManagerController {

    @Autowired
    private IUnitService unitNewService;

    @Autowired
    private IFlowUnitService flowUnitService;

    @Resource
    private FlowUnitMapper flowUnitMapper;

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

    @GetMapping("/select")
    private Result select() {
        // 1. 获取所有单位数据
        Result result = unitNewService.selectAllQuery();
        List<Unit> allUnits = (List<Unit>) result.getData();

        // 2. 获取已关联的 unitId
        Set<Long> associatedUnitIds = flowUnitMapper.selectAllQuery().stream()
                .map(FlowUnit::getUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());

        // 3. 过滤未关联的单位
        List<Unit> filteredUnits = allUnits.stream()
                .filter(unit -> unit.getId() != null && !associatedUnitIds.contains(unit.getId()))
                .collect(Collectors.toList());
        result.setData(filteredUnits);
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
