package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.Dhcp6Dto;
import com.metoo.nrsm.core.service.IDhcp6Service;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.Dhcp;
import com.metoo.nrsm.entity.Dhcp6;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 17:06
 */
@RequestMapping("/admin/dhcp6")
@RestController
public class Dhcp6ManagerController {

    @Autowired
    private IDhcp6Service dhcp6Service;

    @PostMapping("/list")
    public Result list(@RequestBody Dhcp6Dto dto){
        Page<Dhcp6> page = this.dhcp6Service.selectConditionQuery(dto);
        if(page.getResult().size() > 0) {
            return ResponseUtil.ok(new PageInfo<Dhcp>(page));
        }
        return ResponseUtil.ok();
    }

    @PostMapping("/get")
    public void get(){
        this.dhcp6Service.gather(new Date());
    }

}
