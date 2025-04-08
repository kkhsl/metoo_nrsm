package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.core.vo.LicenseVo;
import com.metoo.nrsm.core.vo.MenuVo;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 15:23
 */
@RequestMapping("/admin/index")
@RestController
public class IndexManagerController {

    @Autowired
    private INetworkElementService networkElementService;
    @Autowired
    private IFlowStatisticsService flowStatisticsService;
    @Autowired
    private IIndexService indexService;
    @Autowired
    private ISysConfigService configService;
    @Autowired
    private ILicenseService licenseService;
    @Autowired
    private AesEncryptUtils aesEncryptUtils;

    @GetMapping("/flux/device")
    public Object device(){
        Map params = new HashMap();
        params.put("isipv6", true);
        List<NetworkElement> v4ip_v6ip_count = networkElementService.selectObjByMap(params);
        params.clear();
        params.put("isipv6", false);
        List<NetworkElement> ipv4_count = networkElementService.selectObjByMap(params);
        Map result = new HashMap();
        result.put("v4ip_v6ip_count", v4ip_v6ip_count.size());
        result.put("ipv4_count", ipv4_count.size());

//        result.put("v4ip_v6ip_count", 163);
//        result.put("ipv4_count", 0);

        return ResponseUtil.ok(result);

    }

    @GetMapping("/flux/statistics")
    public Object statistics(){
        Map params = new HashMap();
        params.put("startOfDay", DateTools.getStartOfDay());
        params.put("endOfDay", DateTools.getEndOfDay());
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
        return ResponseUtil.ok(flowStatisticsList);
    }

    @ApiOperation("系统导航")
    @RequestMapping("/nav")
    public Object nav(){
        Map map = new HashMap();
        User user = ShiroUserHolder.currentUser();
        List<MenuVo> menuList = this.indexService.findMenu(user.getId());
        map.put("obj", menuList);
        SysConfig configs = this.configService.select();
        map.put("domain", configs.getDomain());
        List<License> licenses = this.licenseService.query();
        map.put("licenseAC", false);
        if(licenses.size() > 0){
            try {
                String licenseInfo = this.aesEncryptUtils.decrypt(licenses.get(0).getLicense());
                LicenseVo license = JSONObject.parseObject(licenseInfo, LicenseVo.class);
                map.put("licenseAC", license.isLicenseAC());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return ResponseUtil.ok(map);
    }
}
