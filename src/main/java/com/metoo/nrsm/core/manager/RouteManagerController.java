package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.impl.Route6ServiceImpl;
import com.metoo.nrsm.core.service.impl.RouteServiceImpl;
import com.metoo.nrsm.entity.NetworkElement;
import io.swagger.annotations.Api;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api("路由表管理")
@RequestMapping("/admin/route")
@RestController
public class RouteManagerController {

    @Autowired
    private INetworkElementService networkElementService;

    @Autowired
    private RouteServiceImpl routeService;
    @Autowired
    private Route6ServiceImpl route6Service;




    @RequestMapping("/collect")
    public String collectSingleDevice() {
        List<NetworkElement> devices = networkElementService.selectConditionByIpQuery(null);
        devices.forEach(routeService::processDeviceRoutes);
        return "已触发所有设备路由收集: " + devices.size() + "台设备";
    }

    @RequestMapping("/collectV6")
    public String collectDevice() {
        List<NetworkElement> devices = networkElementService.selectConditionByIpQuery(null);
        devices.forEach(route6Service::processDeviceRoutes6);
        return "已触发所有设备路由收集: " + devices.size() + "台设备";
    }


}
