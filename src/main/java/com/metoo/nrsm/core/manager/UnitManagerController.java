package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.FlowUnit;
import com.metoo.nrsm.entity.Unit;
import com.metoo.nrsm.entity.UnitSubnet;
import com.metoo.nrsm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RequestMapping("/admin/unit")
@RestController
public class UnitManagerController {

    @Autowired
    private IUnitService unitNewService;
    @Autowired
    private IUserService userService;
    @Autowired
    private TerminalUnitMapper terminalUnitMapper;
    @Autowired
    private IFlowUnitService flowUnitService;

    @PostMapping("/list")
    public Result list(@RequestBody(required=false) UnitNewDTO dto){
        Result result = this.unitNewService.selectObjConditionQuery(dto);
        return result;
    }

    @GetMapping("/selectAll")
    public Result selectAll(){
        Result result = this.unitNewService.selectAllQuery();
        return result;
    }

    @PostMapping("/save")
    public Result save(@RequestBody Unit instance){
        Result result = this.unitNewService.save(instance);
        return result;
    }

    @DeleteMapping("/delete")
    @Transactional(propagation = Propagation.REQUIRED, timeout = 30)
    public Result delete(@RequestParam String ids) {
        if (ids == null || ids.isEmpty()) {
            return ResponseUtil.badArgument("参数错误");
        }

        List<Long> checkIds = new ArrayList<>();
        for (String idStr : ids.split(",")) {
            try {
                checkIds.add(Long.parseLong(idStr.trim()));
            } catch (NumberFormatException e) {
                return ResponseUtil.badArgument("非法ID格式: " + idStr);
            }
        }

        List<Unit> unitsToDelete = new ArrayList<>();
        List<String> errors = new ArrayList<>();

        for (Long id : checkIds) {
            Unit unit = unitNewService.selectObjById(id);
            if (unit == null) {
                errors.add("单位不存在: " + id);
                continue;
            }

            // 检查关联用户
            UserDto userDto = new UserDto();
            userDto.setUnitId(id);
            Page<User> users = userService.selectObjConditionQuery(userDto);
            if (!users.isEmpty()) {
                errors.add("单位ID " + id + " 存在关联用户禁止删除!");
            }

            // 检查关联子网
            List<UnitSubnet> unitSubnets = terminalUnitMapper.selectByUnitId(id);
            if (!unitSubnets.isEmpty()) {
                errors.add("单位ID " + id + " 存在关联网段禁止删除!");
            }

            // 检查关联流量单位
            List<FlowUnit> flowUnits = flowUnitService.selectByUnitId(id);
            if (!flowUnits.isEmpty()) {
                errors.add("单位ID " + id + " 存在关联流量单位禁止删除!");
            }
            if (errors.isEmpty()) {
                unitsToDelete.add(unit);
            }
        }

        if (!errors.isEmpty()) {
            return ResponseUtil.badArgument(String.join("; ", errors));
        }

        // 执行逻辑删除
        for (Unit unit : unitsToDelete) {
            unit.setDeleteStatus(1);
            unitNewService.update(unit);
        }

        return ResponseUtil.ok("删除成功");
    }

}
