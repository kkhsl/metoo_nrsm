package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.mapper.UnitMapper;

import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit;
import com.metoo.nrsm.entity.UnitSubnet;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;
import java.util.Map;

@RestController
@RequestMapping("/admin/terminal/unit")
public class TerminalUnitManagerController {

    @Resource
    private TerminalUnitMapper terminalUnitMapper;
    @Resource
    private IUnitService unitService;

    @GetMapping("/selectAll")
    public Result selectAll() {
        try {
            List<UnitSubnet> unitSubnets = terminalUnitMapper.selectAll();
            if (unitSubnets == null || unitSubnets.isEmpty()) {
                return ResponseUtil.ok(unitSubnets);
            }
            return ResponseUtil.ok(unitSubnets);
        } catch (Exception e) {
            return ResponseUtil.error("Failed to retrieve data: " + e.getMessage());
        }
    }

    @PostMapping("/saveAll")
    @Transactional
    public Result saveAll(@RequestBody List<UnitSubnet> unitSubnets) {
        if (unitSubnets == null || unitSubnets.isEmpty()) {
            return ResponseUtil.error("Input data cannot be empty");
        }
        if (!unitSubnets.isEmpty()) {
            Map map = new HashMap();
            for (UnitSubnet unitSubnet : unitSubnets) {
                if (map.get(unitSubnet.getUnitId()) != null) {
                    Unit unit2 = this.unitService.selectObjById(unitSubnet.getUnitId());
                    return ResponseUtil.badArgument(unit2.getUnitName() + " 网段名称重复");
                }
                map.put(unitSubnet.getUnitId(), unitSubnet.getUnitId());

            }
        }

        try {
            for (UnitSubnet unitSubnet : unitSubnets) {
                unitSubnet.setAddTime(new Date());
                if (unitSubnet.getId() == null) {
                    // ID 为空，执行插入操作
                    terminalUnitMapper.insert(unitSubnet);
                } else {
                    // ID 存在，检查是否已存在
                    UnitSubnet subnet = terminalUnitMapper.findById(unitSubnet.getId());
                    if (subnet == null) {
                        terminalUnitMapper.insert(unitSubnet);
                    } else {
                        terminalUnitMapper.update(unitSubnet);
                    }
                }
            }
            return ResponseUtil.ok("UnitSubnet added/updated successfully");
        } catch (Exception e) {
            return ResponseUtil.error("Failed to save data: " + e.getMessage());
        }
    }


    @DeleteMapping("/deleteAll")
    public Result deleteAll(@RequestParam(required = true) Long id) {
        try {
            UnitSubnet subnet = terminalUnitMapper.findById(id);
            if (subnet == null) {
                return ResponseUtil.error("UnitSubnet with ID " + id + " does not exist");
            }
            terminalUnitMapper.deleteById(id);
            return ResponseUtil.ok("UnitSubnet deleted successfully");
        } catch (Exception e) {
            return ResponseUtil.error("Failed to delete: " + e.getMessage());
        }
    }


    //@GetMapping("/getTraffic")

}
