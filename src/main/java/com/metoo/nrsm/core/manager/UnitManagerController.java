package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.UnitNewDTO;
import com.metoo.nrsm.core.dto.UserDto;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.service.IUserService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Unit;
import com.metoo.nrsm.entity.User;
import org.springframework.beans.factory.annotation.Autowired;
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
    @Transactional
    public Result delete(@RequestParam String ids) {
        List<Long> checkIds = new ArrayList<>();
        if (ids != null && !ids.isEmpty()) {
            // 先检查所有单位是否可删除
            for (String idStr : ids.split(",")) {
                Long id = Long.valueOf(idStr);
                Unit unit2 = unitNewService.selectObjById(id);
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
                Unit unit2 = unitNewService.selectObjById(id);
                unit2.setDeleteStatus(1);
                unitNewService.update(unit2);
            }
            return ResponseUtil.ok("删除成功");
        }
        return ResponseUtil.badArgument("参数错误");
    }

}
