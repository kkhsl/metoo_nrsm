package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.RadvdDTO;
import com.metoo.nrsm.core.service.IInterfaceService;
import com.metoo.nrsm.core.service.IRadvdService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.Interface;
import com.metoo.nrsm.entity.NetworkElement;
import com.metoo.nrsm.entity.Radvd;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/radvd")
public class RadvdManagerController {

    @Autowired
    private IRadvdService radvdService;
    @Autowired
    private IInterfaceService interfaceService;

    /**
     * 获取列表（分页）
     */
    @PostMapping("/list")
    public Object list(@RequestBody RadvdDTO dto) {
        // 开启分页
        Page<Radvd> page = this.radvdService.selectObjConditionQuery(dto);
        if(page.getResult().size() > 0){
            for (Radvd radvd : page.getResult()) {
                if(radvd.getInterfaceId() != null){
                    Interface obj = this.interfaceService.selectObjById(radvd.getInterfaceId());
                    if(obj != null){
                        radvd.setInterfaceName(obj.getName());
                    }
                }
            }
        }
        Map map = new HashMap<>();
        List<Interface> interfaceList = this.interfaceService.selectObjByMap(Collections.emptyMap());
        map.put("interface", interfaceList);
        return ResponseUtil.ok(new PageInfo<Radvd>(page, map));
    }

    /**
     * 获取详情
     */
    @GetMapping("/{id}")
    public Object get(@PathVariable Long id) {
        Radvd radvd = this.radvdService.selectObjById(id);
        if (radvd == null) {
            return ResponseUtil.badArgument("记录不存在");
        }
        return ResponseUtil.ok(radvd);
    }

    /**
     * 新增
     */
    @PostMapping("/save")
    public Object add(@RequestBody Radvd radvd) {
        // 基本参数验证
        if (radvd.getName() == null || radvd.getName().trim().isEmpty()) {
            return ResponseUtil.badArgument("名称不能为空");
        }
        if (this.radvdService.save(radvd)) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.updatedDataFailed();
    }

    /**
     * 删除
     */
    @DeleteMapping("/{id}")
    public Object delete(@PathVariable Long id) {
        // 验证记录是否存在
        Radvd existing = this.radvdService.selectObjById(id);
        if (existing == null) {
            return ResponseUtil.badArgument("记录不存在");
        }

        if (this.radvdService.delete(id)) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.updatedDataFailed();
    }

}
