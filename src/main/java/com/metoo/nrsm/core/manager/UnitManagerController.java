package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.mapper.TopologyMapper;
import com.metoo.nrsm.core.mapper.UnitMapper;
import com.metoo.nrsm.core.service.IFlowUnitService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.service.impl.AreaServiceImpl;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

@RequestMapping("/admin/unit")
@RestController
public class UnitManagerController {

    @Autowired
    private IUnitService unitService;
    @Autowired
    private IUserService userService;
    @Autowired
    private TerminalUnitMapper terminalUnitMapper;
    @Autowired
    private IFlowUnitService flowUnitService;

    @Autowired
    private TopologyMapper topologyMapper;

    @Autowired
    private AreaServiceImpl areaService;

    @Resource
    private UnitMapper unitMapper;



    @PostMapping("/list")
    public Result list(@RequestBody(required = false) UnitNewDTO dto) {
        Result result = this.unitService.selectObjConditionQuery(dto);
        return result;
    }

    @GetMapping("/selectAll")
    public Result selectAll() {
        Result result = this.unitService.selectAllQuery();
        return result;
    }

    @GetMapping("/selectByUser")
    public Result selectByUser() {
        User user = ShiroUserHolder.currentUser();
        UnitNewDTO unitNewDTO = new UnitNewDTO();

        Unit loginUnit = unitMapper.selectObjById(user.getUnitId());
        if (loginUnit.getUnitLevel()!=null){
            if (loginUnit.getUnitLevel()==0){
                List<Unit> result = unitMapper.selectByUser(unitNewDTO);
                return ResponseUtil.ok(result);
            }else {
                unitNewDTO.setId(user.getUnitId());
                List<Unit> result = unitMapper.selectByUser(unitNewDTO);
                return ResponseUtil.ok(result);
            }
        }else {
            unitNewDTO.setId(user.getUnitId());
            List<Unit> result = unitMapper.selectByUser(unitNewDTO);
            return ResponseUtil.ok(result);
        }


    }

    @GetMapping("/select")
    public Result select() {
        // 1. 获取所有单位数据
        Result result = unitService.selectAllQuery();
        List<Unit> allUnits = (List<Unit>) result.getData();
        // 2. 获取已关联的 unitId
        Set<Long> associatedUnitIds = terminalUnitMapper.selectAll().stream()
                .map(UnitSubnet::getUnitId)
                .filter(Objects::nonNull)
                .collect(Collectors.toSet());
        // 3. 过滤未关联的单位
        List<Unit> filteredUnits = allUnits.stream()
                .filter(unit -> unit.getId() != null && !associatedUnitIds.contains(unit.getId()))
                .collect(Collectors.toList());
        // 4. 更新结果
        result.setData(filteredUnits);
        return result;
    }

    @GetMapping("/tree")
    public Result getAreaTree() {
        List<Area> areaTree = areaService.getFullAreaTree();
        return ResponseUtil.ok(areaTree);
    }


    @PostMapping("/save")
    public Result save(@RequestBody Unit instance) {
        Result result = this.unitService.save(instance);
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
            Unit unit = unitService.selectObjById(id);
            if (unit == null) {
                errors.add("单位不存在: " + id);
                continue;
            }

            //检查关联拓扑
            Map map=new HashMap();
            map.put("isDefault",1);
            map.put("isDefault",id);
            List<Topology> topologies = topologyMapper.selectObjByMap(map);
            if (!topologies.isEmpty()) {
                errors.add("单位ID " + id + " 存在关联拓扑禁止删除!");
            }

            // 检查关联用户
            UserDto userDto = new UserDto();
            userDto.setUnitId(id);
            Page<User> users = userService.selectObjConditionQuery(userDto);
            if (!users.isEmpty()) {
                errors.add("单位ID " + id + " 存在关联用户禁止删除!");
            }

            // 检查关联流量单位
            List<FlowUnit> flowUnits = flowUnitService.selectByUnitId(id);
            if (!flowUnits.isEmpty()) {
                errors.add("单位ID " + id + " 存在关联流量单位禁止删除!");
            }

            // 没有错误则添加待删除列表
            if (errors.isEmpty()) {
                unitsToDelete.add(unit);
            }
        }

        if (!errors.isEmpty()) {
            return ResponseUtil.badArgument(String.join("; ", errors));
        }

        // 逐个删除单位对应的网段条目
        for (Unit unit : unitsToDelete) {
            Long unitId = unit.getId();

            // 获取该单位的所有网段条目
            List<UnitSubnet> subnets = terminalUnitMapper.selectByUnitId(unitId);

            // 逐个删除每个网段条目
            for (UnitSubnet subnet : subnets) {
                terminalUnitMapper.deleteById(subnet.getId()); // 通过ID删除
            }

            // 执行单位逻辑删除
            unit.setDeleteStatus(1);
            unitService.update(unit);
        }

        return ResponseUtil.ok("删除成功");
    }

}
