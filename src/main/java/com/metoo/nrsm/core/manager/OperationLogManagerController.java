package com.metoo.nrsm.core.manager;

import com.github.pagehelper.Page;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.dto.OperationLogDTO;
import com.metoo.nrsm.core.service.IOperationLogService;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.query.PageInfo;
import com.metoo.nrsm.entity.OperationLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

/**
 * @author HKK
 * @version 1.0
 * @date 2023-11-07 16:32
 */
@RequestMapping("/admin/operation/log")
@RestController
public class OperationLogManagerController {

    @Autowired
    private IOperationLogService operationLogService;

    @PostMapping("/list")
    public Object list(@RequestBody OperationLogDTO dto) {
        Page<OperationLog> page = this.operationLogService.selectObjConditionQuery(dto);
        if (page.getResult().size() > 0) {
            return ResponseUtil.ok(new PageInfo<OperationLog>(page));
        }
        return ResponseUtil.ok();
    }

    @PostMapping("/save")
    public Object save(HttpServletRequest request, @RequestBody OperationLog instance) {
        instance.setType(1);
//        instance.setIp(request.getRemoteAddr());
        instance.setIp(Ipv4Util.getRealIP(request));
        boolean flag = this.operationLogService.save(instance);
        if (flag) {
            return ResponseUtil.ok();
        }
        return ResponseUtil.error();
    }

    @DeleteMapping("/delete")
    public Object delete(@RequestParam String id) {
        String[] dms = id.split(",");
        if (dms.length > 1) {
            for (String s : dms) {
                boolean flag = this.operationLogService.delete(Long.parseLong(s));
                if (flag) {
                    continue;
                } else {
                    OperationLog operationLog = this.operationLogService.selectObjById(Long.parseLong(s));
                    if (operationLog != null) {
                        return ResponseUtil.error(operationLog.getAccount() + "删除失败");
                    }
                    continue;
                }
            }
            return ResponseUtil.ok();
        } else {
            boolean flag = this.operationLogService.delete(Long.parseLong(id));
            if (flag) {
                return ResponseUtil.ok();
            }
            return ResponseUtil.error();
        }
    }

}
