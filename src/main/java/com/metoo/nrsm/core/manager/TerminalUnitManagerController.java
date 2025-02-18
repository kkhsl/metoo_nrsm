package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ITerminalUnitService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.TerminalUnit;
import com.metoo.nrsm.entity.TerminalUnitSubnet;
import com.metoo.nrsm.entity.TerminalUnitSubnetV6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin/terminal/unit")
public class TerminalUnitManagerController {

    @Autowired
    private ITerminalUnitService terminalUnitService;

    @GetMapping("/all")
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
    }

}
