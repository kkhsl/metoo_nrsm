package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.dto.ProjectDTO;
import com.metoo.nrsm.core.service.IProjectService;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.Project;
import com.metoo.nrsm.entity.User;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@ApiOperation("项目管理")
@RequestMapping("/admin/project")
@RestController
public class ProjectManagerController {

    @Autowired
    private IProjectService projectService;

    @RequestMapping("/list")
    public Object list(@RequestBody(required = false) ProjectDTO dto) {
        Page<Project> page = this.projectService.selectObjConditionQuery(dto);
        if (page.getResult().size() > 0) {
            return ResponseUtil.ok(new PageInfo<Project>(page));
        }
        return ResponseUtil.ok();
    }

    @GetMapping
    public Object update(@RequestParam(value = "id") Long id) {
        Project project = this.projectService.selectObjById(id);
        if (project != null) {
            return ResponseUtil.ok(project);
        }
        return ResponseUtil.badArgument();
    }

    @PostMapping
    public Object save(@RequestBody Project instance) {
        // 校验参数
        Map params = new HashMap();
        params.put("projectId", instance.getId());
        params.put("name", instance.getName());
        List<Project> projectList = this.projectService.selectObjByMap(params);
        if (projectList.size() > 0) {
            return ResponseUtil.badArgument("项目名称重复");
        }
        if (instance.getStartTime() != null && instance.getAcceptTime() != null) {
            if (instance.getStartTime().after(instance.getAcceptTime())) {
                return ResponseUtil.badArgument("项目启动时间需小于项目验收时间");
            }
        }
        int result = this.projectService.save(instance);
        if (result >= 1) {
            return ResponseUtil.ok();
        } else {
            return ResponseUtil.error("无权限修改！");
        }
    }

    @DeleteMapping
    public Object delete(@RequestParam(value = "ids") String ids) {
        for (String id : ids.split(",")) {
            Project project = this.projectService.selectObjById(Long.parseLong(id));
            if (project != null) {
                int delete = this.projectService.delete(Long.parseLong(id));
                if (delete >= 1) {
                    return ResponseUtil.ok();
                } else {
                    return ResponseUtil.error("无权限删除！");
                }
            } else {
                return ResponseUtil.error();
            }
        }
        return ResponseUtil.ok();
    }

}
