package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.ShiroUserHolder;
import com.metoo.nrsm.core.enums.license.FeatureModule;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.license.AesEncryptUtils;
import com.metoo.nrsm.core.vo.LicenseVo;
import com.metoo.nrsm.core.vo.MenuVo;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.*;
import io.swagger.annotations.ApiOperation;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.stream.Collectors;

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
    private SnmpStatusUtils snmpStatusUtils;

    @GetMapping("/flux/device")
    public Object device() {
//        Map params = new HashMap();
//        params.put("isipv6", true);
//        List<NetworkElement> v4ip_v6ip_count = networkElementService.selectObjByMap(params);
//        params.clear();
//        params.put("isipv6", false);
//        List<NetworkElement> ipv4_count = networkElementService.selectObjByMap(params);
//        Map result = new HashMap();
//        result.put("v4ip_v6ip_count", v4ip_v6ip_count.size());
//        result.put("ipv4_count", ipv4_count.size());

//        result.put("v4ip_v6ip_count", 163);
//        result.put("ipv4_count", 0);

        List<NetworkElement> networkElements = this.getGatherDevice();

//        Map<String, Long> stats = networkElements.stream()
//                .collect(Collectors.groupingBy(
//                        ne -> {
//                            boolean hasIp = ne.getIp() != null && !ne.getIp().isEmpty();
//                            boolean hasIpv6 = ne.getV6ip() != null && !ne.getV6ip().isEmpty();
//
//                            if (hasIp && hasIpv6) return "v4ip_v6ip_count";
//                            if (hasIp) return "ipv4_count";
//                            if (hasIpv6) return "ipv6_count";
//                            return "noip_count";
//                        },
//                        Collectors.counting()
//                ));
//
//        stats.forEach((k, v) -> System.out.println(k + ": " + v));

        Map<String, Long> stats = new LinkedHashMap<>();
        stats.put("ipv4_count", 0L);
        stats.put("ipv6_count", 0L);
        stats.put("v4ip_v6ip_count", 0L);
        stats.put("noip_count", 0L);

        networkElements.forEach(ne -> {
            boolean hasIp = ne.getIp() != null && !ne.getIp().isEmpty();
//            boolean hasIpv6 = ne.isIsipv6() != null && !ne.getV6ip().isEmpty();
            boolean hasIpv6 = ne.isIsipv6();

            String key;
            if (hasIp && hasIpv6) {
                key = "v4ip_v6ip_count";
            } else if (hasIp) {
                key = "ipv4_count";
            } else if (hasIpv6) {
                key = "ipv6_count";
            } else {
                key = "noip_count";
            }

            stats.merge(key, 1L, Long::sum); // 如果 key 存在，则值 +1；否则初始化为 1
        });

        return ResponseUtil.ok(stats);

    }

    // 获取需要采集的设备
    public List<NetworkElement> getGatherDevice() {
        List<NetworkElement> networkElements = new ArrayList<>();
        Set<String> uuids = this.snmpStatusUtils.getOnlineDevice();
        if (uuids.size() > 0) {
            for (String uuid : uuids) {
                NetworkElement networkElement = this.networkElementService.selectObjByUuid(uuid);
                if (networkElement != null
                        && StringUtils.isNotBlank(networkElement.getIp())
                        && StringUtils.isNotBlank(networkElement.getVersion())
                        && StringUtils.isNotBlank(networkElement.getCommunity())) {
                    networkElements.add(networkElement);
                }
            }
        }
        return networkElements;
    }

    @GetMapping("/flux/statistics")
    public Object statistics() {
        Map params = new HashMap();
        params.put("startOfDay", DateTools.getStartOfDay());
        params.put("endOfDay", DateTools.getEndOfDay());
        List<FlowStatistics> flowStatisticsList = this.flowStatisticsService.selectObjByMap(params);
        return ResponseUtil.ok(flowStatisticsList);
    }

    @ApiOperation("系统导航")
    @RequestMapping("/nav")
    public Object nav() {
        Map map = new HashMap();
        User user = ShiroUserHolder.currentUser();
//        map.put("obj", new ArrayList<>());
//        if(user != null){
//            map.put("obj", this.indexService.findMenu(user.getId()));
//        }
        map.put("obj", this.indexService.findMenu(user.getId()));

        SysConfig configs = this.configService.select();
        map.put("domain", configs.getDomain());

        map.put("versionType", getLicenseType());// 改用probe

        return ResponseUtil.ok(map);
    }

    public boolean getLicenseType(){

        List<License> licenses = licenseService.query();
        if(licenses.size() > 0){
            License obj = licenseService.query().get(0);
            String uuid = SystemInfoUtils.getSerialNumber();
            if (uuid.equals(obj.getSystemSN()) && obj.getStatus() == 0 && (obj.getLicense() != null && !"".equals(obj.getLicense()))) {
                String licenseInfo = AesEncryptUtils.decrypt(obj.getLicense());
                LicenseVo licenseVo = JSONObject.parseObject(licenseInfo, LicenseVo.class);
                if(licenseVo.getFeatureModules().contains(FeatureModule.ASSET_SCAN)){
                    return true;
                }
            }
        }
        return false;
    }
}
