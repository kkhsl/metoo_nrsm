package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.Dhcp6Dto;
import com.metoo.nrsm.core.dto.DhcpDto;
import com.metoo.nrsm.core.service.IDhcp6Service;
import com.metoo.nrsm.core.service.IDhcpService;
import com.metoo.nrsm.core.utils.dhcp.DhcpUtils;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.nspm.AddressPool;
import com.metoo.nrsm.entity.nspm.Dhcp;
import com.metoo.nrsm.entity.nspm.Dhcp6;
import com.metoo.nrsm.entity.nspm.Internet;
import org.apache.commons.lang3.StringUtils;
import org.junit.Test;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cglib.beans.BeanMap;
import org.springframework.data.repository.init.ResourceReader;
import org.springframework.web.bind.annotation.*;

import java.io.*;
import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

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
