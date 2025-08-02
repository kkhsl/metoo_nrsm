package com.metoo.nrsm.core.manager;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.ResDto;
import com.metoo.nrsm.core.service.IResService;
import com.metoo.nrsm.entity.Res;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Api("权限管理")
@RestController
@RequestMapping("/admin/permission")
public class ResManagerController {

    private final IResService resService;

    @Autowired
    public ResManagerController(IResService resService) {
        this.resService = resService;
    }

    @RequiresPermissions("LK:PERMISSION:MANAGER")
    @ApiOperation("权限列表")
    @PostMapping("/list")
    public Object list(@RequestBody ResDto dto) {
        Map data = new HashMap();
        if (dto == null) {
            dto = new ResDto();
        }
        if (dto.getCurrentPage() < 1) {
            dto.setCurrentPage(0);
        }
        Map params = new HashMap();
        params.put("currentPage", dto.getCurrentPage() - 1);
        params.put("pageSize", dto.getPageSize());
        List<Res> ResList = this.resService.findPermissionByJoin(params);
        data.put("obj", ResList);
        data.put("currentPage", dto.getCurrentPage());
        data.put("pageSize", ResList.size());
        return ResponseUtil.ok(data);
    }

    @RequiresPermissions("LK:PERMISSION:MANAGER")
    @ApiOperation("权限添加")
    @PostMapping("/add")
    public Object add() {
        Map data = new HashMap();
        Map<String, Integer> params = Collections.singletonMap("level", 0);
        List<Res> parentList = this.resService.findPermissionByMap(params);
        data.put("parentList", parentList);
        return ResponseUtil.ok(data);
    }

    @RequiresPermissions("LK:PERMISSION:MANAGER")
    @ApiOperation("权限更新")
    @PostMapping("/update")
    public Object update(@RequestBody ResDto dto) {
        Map existingResource = new HashMap();
        Res topLevelPermissions = this.resService.findResUnitRoleByResId(dto.getId());
        if (topLevelPermissions == null) {
            return ResponseUtil.badArgument("指定的权限资源不存在");
        }
        existingResource .put("obj", topLevelPermissions );
        Map<String, Integer> params = Collections.singletonMap("level", 0);
        List<Res> parentList = this.resService.findPermissionByMap(params);
        existingResource .put("parentList", parentList);
        return ResponseUtil.ok(existingResource);
    }

    @ApiOperation("权限信息查询")
    @RequiresPermissions("LK:PERMISSION:MANAGER")
    @RequestMapping("/query")
    public Object query(@RequestBody ResDto dto) {
        if (dto.getId() != null) {
            Res res = this.resService.findObjById(dto.getId());
            if (res != null) {
                Map params = new HashMap();
                params.put("parentId", res.getId());
                List<Res> resList = this.resService.findPermissionByMap(params);
                return ResponseUtil.ok(resList);
            }
        }
        return ResponseUtil.badArgument();
    }

    public Object verify(ResDto dto) {
        String name = dto.getName();
        if (name == null || name.equals("")) {
            return ResponseUtil.badArgument();
        }
        return null;
    }

    @RequiresPermissions("LK:PERMISSION:MANAGER")
    @ApiOperation("权限保存")
    @PostMapping("/save")
    public Object save(@RequestBody ResDto dto) {
        if (dto != null) {
            Object error = this.verify(dto);
            if (error != null) {
                return error;
            }
            if (dto.getParentId() != null && StringUtil.isEmpty(dto.getValue())) {
                return ResponseUtil.badArgument("资源信息为空");
            }
            boolean flag = true;
            Map map = new HashMap();
            map.put("name", dto.getName());
            if (dto.getParentId() != null) {
                map.put("level", 1);
            } else {
                map.put("level", 0);
            }
            Res res = this.resService.findObjByNameAndLevel(map);
            if (res != null) {
                flag = false;
                Res res2 = this.resService.findObjById(dto.getId());
                if (res2 != null) {
                    if (res2 != null && !res.getName().equals(res2.getName())) {
                        flag = false;
                    } else {
                        flag = true;
                    }
                }
            }
            if (flag) {
                if (dto.getParentId() != null) {
                    Res resParent = this.resService.findObjById(dto.getParentId());
                    if (resParent == null) {
                        return ResponseUtil.badArgument("填写正确的父级ID");
                    }
                    dto.setLevel(1);
                }
                if (this.resService.save(dto)) {
                    return ResponseUtil.ok();
                }
            }
            return ResponseUtil.badArgument("权限已存在");
        }
        return ResponseUtil.badArgument();
    }

    @RequiresPermissions("LK:PERMISSION:MANAGER")
    @ApiOperation("权限删除")
    @PostMapping("/delete")
    public Object delete(@RequestBody ResDto dto) {
        Res res = this.resService.findObjById(dto.getId());
        if (res != null) {
            Map params = new HashMap();
            params.put("parentId", res.getId());
            List<Res> resList = this.resService.findPermissionByMap(params);
            if (resList.size() > 0) {
                // ** 可优化为批量删除，首先获取所有子集IdList
                for (Res obj : resList) {
                    params.clear();
                    params.put("parentId", obj.getId());
                    if (resList.size() > 0) {
                        for (Res obj1 : resList) {
                            this.resService.delete(obj1.getId());
                        }
                    }
                    this.resService.delete(obj.getId());
                }
            }
            if (this.resService.delete(res.getId())) {
                return ResponseUtil.ok();
            }
            return ResponseUtil.error();
        }
        return ResponseUtil.badArgument();
    }

}
