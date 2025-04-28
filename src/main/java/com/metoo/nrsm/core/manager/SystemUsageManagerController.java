package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.ISystemUsageService;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.entity.SystemUsage;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/system/usage")
public class SystemUsageManagerController {

    @Autowired
    private ISystemUsageService systemUsageService;

    @GetMapping("/list")
    public Result list(@RequestParam(name = "startTime", required = false) String startTime,
                       @RequestParam(name = "endTime", required = false) String endTime) {
        if(StringUtils.isEmpty(startTime) || StringUtils.isEmpty(endTime)){
            return ResponseUtil.badArgument("参数错误");
        }

        // 定义日期时间格式
        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            LocalDateTime start = (startTime != null) ? LocalDateTime.parse(startTime, formatter) : null;
            LocalDateTime end = (endTime != null) ? LocalDateTime.parse(endTime, formatter) : null;

            // 检查 startTime 和 endTime 的关系
            if (start != null && end != null && end.isBefore(start)) {
                return ResponseUtil.badArgument("起始时间必须大于结束时间");
            }
        } catch (Exception e) {
            return ResponseUtil.badArgument("日期格式无效");
        }


        // 根据时间范围查询逻辑
        Map params = new HashMap();
        params.put("startTime", startTime);
        params.put("endTime", endTime);
        List<SystemUsage> list = systemUsageService.selectObjByMap(params);
        return ResponseUtil.ok(list);
    }
}
