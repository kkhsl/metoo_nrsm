package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.mapper.TerminalUnitMapper;
import com.metoo.nrsm.core.service.ITerminalUnitService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.UnitSubnet;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import java.util.*;

@RestController
@RequestMapping("/admin/terminal/unit")
public class TerminalUnitManagerController {

    @Autowired
    private ITerminalUnitService terminalUnitService;

    @Resource
    private TerminalUnitMapper terminalUnitMapper;



    @GetMapping("/selectAll")
    public Result selectAll() {
        try {
            List<UnitSubnet> unitSubnets = terminalUnitMapper.selectAll();
            if (unitSubnets == null || unitSubnets.isEmpty()) {
                return ResponseUtil.ok("No data found");
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

        try {
            for (UnitSubnet unitSubnet : unitSubnets) {
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




    /*@GetMapping("/all")
    public Result all(){
        List<TerminalUnit> terminalUnitList = this.terminalUnitService.selectObjAll();
        List<Map<String, Object>> filteredList = terminalUnitList.stream()
                .map(unit -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("name", unit.getName());
                    map.put("terminaV4lList", unit.getTerminaV4lList());
                    map.put("terminaV6lList", unit.getTerminaV6lList());
                    map.put("id", unit.getId());
                    map.put("addTime", unit.getAddTime());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseUtil.ok(filteredList);
    }

    @PostMapping("/save")
    public Result save(@RequestBody List<TerminalUnit> terminalUnits) {
        // 检查终端单位列表是否为空
        if (terminalUnits == null || terminalUnits.isEmpty()) {
            return ResponseUtil.fail("TerminalUnit list cannot be null or empty");
        }

        // 遍历每个 TerminalUnit
        for (TerminalUnit terminalUnit : terminalUnits) {
            // 检查单个 TerminalUnit 是否为空
            if (terminalUnit == null) {
                return ResponseUtil.fail("TerminalUnit cannot be null");
            }

            // 检查名称
            if (terminalUnit.getName() == null || terminalUnit.getName().isEmpty()) {
                return ResponseUtil.fail("TerminalUnit name cannot be null or empty");
            }

            try {
                // 处理 IPv4 子网列表
                List<TerminalUnitSubnet> terminaV4lList = terminalUnit.getTerminaV4lList();
                if (terminaV4lList != null) {
                    for (TerminalUnitSubnet terminalUnitSubnet : terminaV4lList) {
                        terminalUnitSubnet.setAddTime(new Date());
                        terminalUnitService.addV4(terminalUnitSubnet);
                    }
                }

                // 处理 IPv6 子网列表
                List<TerminalUnitSubnetV6> terminaV6lList = terminalUnit.getTerminaV6lList();
                if (terminaV6lList != null) {
                    for (TerminalUnitSubnetV6 terminalUnitSubnetV6 : terminaV6lList) {
                        terminalUnitSubnetV6.setAddTime(new Date());
                        terminalUnitService.addV6(terminalUnitSubnetV6);
                    }
                }

                // 设置添加时间并保存 TerminalUnit
                terminalUnit.setAddTime(new Date());
                terminalUnitService.add(terminalUnit);
            } catch (Exception e) {
                // 记录异常并返回错误信息
                System.err.println("Error processing TerminalUnit: " + terminalUnit.getName());
                e.printStackTrace();
                return ResponseUtil.fail("Error processing TerminalUnit: " + terminalUnit.getName());
            }
        }

        // 返回成功响应
        return ResponseUtil.ok("TerminalUnits added/updated successfully");
    }




    @PostMapping("/add")
    public Result add(@RequestBody List<TerminalUnit> terminalUnits) {
        if (terminalUnits == null || terminalUnits.isEmpty()) {
            return ResponseUtil.fail("TerminalUnit list cannot be null or empty");
        }

        for (TerminalUnit terminalUnit : terminalUnits) {
            if (terminalUnit == null || terminalUnit.getName() == null || terminalUnit.getName().isEmpty()) {
                return ResponseUtil.fail("TerminalUnit name cannot be null or empty");
            }
            terminalUnit.setAddTime(new Date());
            terminalUnitService.add(terminalUnit);
        }

        return ResponseUtil.ok("TerminalUnits added/updated successfully");
    }

    @DeleteMapping("/delete")
    public Result delete(Long id) {
        if (id!=null){
            terminalUnitService.delete(id);
            return ResponseUtil.ok("TerminalUnit delete successfully");
        }
        return ResponseUtil.error("参数不能为空");
    }

    @PostMapping("/addV4")
    public Result addSubnetV4(@RequestBody List<TerminalUnitSubnet> terminalUnitSubnets) {
        // 进行有效性检查
        if (terminalUnitSubnets == null || terminalUnitSubnets.isEmpty()) {
            return ResponseUtil.fail("addSubnetV4 cannot be null or empty");
        }

        for (TerminalUnitSubnet terminalUnitSubnet : terminalUnitSubnets) {
            if (terminalUnitSubnet == null || terminalUnitSubnet.getIp() == null || terminalUnitSubnet.getIp().isEmpty()) {
                return ResponseUtil.fail("Subnet IP cannot be null or empty");
            }
            terminalUnitSubnet.setAddTime(new Date());
            terminalUnitService.addV4(terminalUnitSubnet);
        }

        // 返回成功响应
        return ResponseUtil.ok("TerminalUnitV4 added/updated successfully");
    }

    @DeleteMapping("/deleteV4")
    public Result deleteVV4(Long id) {
        // 进行有效性检查
        if (id!=null){
            terminalUnitService.deleteV4(id);
            return ResponseUtil.ok("TerminalUnitV4 delete successfully");
        }
        return ResponseUtil.error("参数不能为空");
    }




    @PostMapping("/addV6")
    public Result addSubnetV6(@RequestBody List<TerminalUnitSubnetV6> terminalUnitSubnetsV6) {
        // 进行有效性检查
        if (terminalUnitSubnetsV6 == null || terminalUnitSubnetsV6.isEmpty()) {
            return ResponseUtil.fail("addSubnetV6 cannot be null or empty");
        }

        for (TerminalUnitSubnetV6 terminalUnitSubnetV6 : terminalUnitSubnetsV6) {
            if (terminalUnitSubnetV6 == null || terminalUnitSubnetV6.getIp() == null || terminalUnitSubnetV6.getIp().isEmpty()) {
                return ResponseUtil.fail("Subnet IP cannot be null or empty");
            }
            terminalUnitSubnetV6.setAddTime(new Date());
            terminalUnitService.addV6(terminalUnitSubnetV6);
        }

        // 返回成功响应
        return ResponseUtil.ok("TerminalUnitV6 added/updated successfully");
    }

    @DeleteMapping("/deleteV6")
    public Result deleteV6(Long id) {
        // 进行有效性检查
        if (id!=null){
            terminalUnitService.deleteV6(id);
            return ResponseUtil.ok("TerminalUnitV6 delete successfully");
        }
        return ResponseUtil.error("参数不能为空");
    }*/




}
