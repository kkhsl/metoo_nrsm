package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.service.IDeviceTypeService;
import com.metoo.nrsm.core.service.ITerminalTypeService;
import com.metoo.nrsm.entity.DeviceType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/admin/terminal/type")
public class TerminalTypeManagerController {

    @Autowired
    private ITerminalTypeService terminalTypeService;
    @Autowired
    private IDeviceTypeService deviceTypeService;

    @GetMapping
    public Object terminal(){
        Map params = new HashMap();
        params.put("diff", 1);
        params.put("orderBy", "sequence");
        params.put("orderType", "DESC");
        List<DeviceType> deviceTypes = this.deviceTypeService.selectObjByMap(params);
        return ResponseUtil.ok(deviceTypes);
    }

}
