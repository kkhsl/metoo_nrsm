package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitDTO;
import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.service.IUnit2Service;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit2;
import com.metoo.nrsm.entity.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@RequestMapping("/admin/unit")
@RestController
public class Unit2ManagerController {

    @Autowired
    private IUnit2Service unit2Service;
    @Autowired
    private IUserService userService;


    @PostMapping("/list")
    private Result list(@RequestBody UnitDTO dto){
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
    @Transactional
    public Result delete(@RequestParam String ids) {
        List<Long> checkIds = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            // 先检查所有单位是否可删除
            for (String idStr : ids.split(",")) {
                Long id = Long.valueOf(idStr);
                Unit2 unit2 = unit2Service.selectObjById(id);
                if (unit2 == null) {
                    return ResponseUtil.badArgument("单位不存在: " + id);
                }
                UserDto userDto = new UserDto();
                userDto.setUnitId(id);
                Page<User> users = userService.selectObjConditionQuery(userDto);
                if (!users.isEmpty()) {
                    return ResponseUtil.badArgument("单位存在用户禁止删除!");
                }
                checkIds.add(id);
            }
            // 如果所有检查通过，执行删除
            for (Long id : checkIds) {
                Unit2 unit2 = unit2Service.selectObjById(id);
                unit2.setDeleteStatus(1);
                unit2Service.update(unit2);
            }
            return ResponseUtil.ok("删除成功");
        }
        return ResponseUtil.badArgument("参数错误");
    }

}
