package com.metoo.nrsm.core.manager;

import com.metoo.nrsm.core.service.INetworkElementService;
import com.metoo.nrsm.core.service.impl.Route6ServiceImpl;
import com.metoo.nrsm.core.service.impl.RouteServiceImpl;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.entity.NetworkElement;
import io.swagger.annotations.Api;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Date;
import java.util.List;

@Api("路由表管理")
@RequestMapping("/admin/route")
@RestController
@Slf4j
public class RouteManagerController {

    @Value("${task.switch.is-open}")
    private boolean flag;


    @Autowired
    private INetworkElementService networkElementService;

    @Autowired
    private RouteServiceImpl routeService;
    @Autowired
    private Route6ServiceImpl route6Service;

    private volatile boolean isRunningRoute = false;

    @RequestMapping("/collect")
    @Scheduled(fixedDelay = 180_000)
    public void collectSingleDevice() {
        if (flag && !isRunningRoute) {
            log.info("Route采集任务开始");
            isRunningRoute = true;
            try {
                Long time = System.currentTimeMillis();
                Date date = DateTools.gatherDate();
                List<NetworkElement> devices = networkElementService.selectConditionByIpQuery(null);
                for (NetworkElement device : devices) {
                    routeService.processDeviceRoutes(device,date);
                }
                routeService.copyDataToRouteHistory();
                log.info("Route采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Route采集任务异常: {}", e.getMessage());
            } finally {
                isRunningRoute = false;
            }
        }
    }

    private volatile boolean isRunningRoute6 = false;


    @RequestMapping("/collectV6")
    @Scheduled(fixedDelay = 180_000)
    public void collectDevice() {
        if (flag && !isRunningRoute6) {
            log.info("Route6采集任务开始");
            isRunningRoute6 = true;
            try {
                Long time = System.currentTimeMillis();
                Date date = DateTools.gatherDate();
                List<NetworkElement> devices = networkElementService.selectConditionByIpQuery(null);
                for (NetworkElement device : devices) {
                    route6Service.processDeviceRoutes6(device,date);
                }
                route6Service.copyDataToRoute6History();
                log.info("Route6采集时间:{}", DateTools.measureExecutionTime(System.currentTimeMillis() - time));
            } catch (Exception e) {
                e.printStackTrace();
                log.error("Route6采集任务异常: {}", e.getMessage());
            } finally {
                isRunningRoute6 = false;
            }
        }
    }


}
