package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.application.ApplicationContextUtils;
import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.config.utils.CopyPropertiesReflect;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.gather.common.PyCommandBuilder;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.GatherFactory;
import com.metoo.nrsm.core.config.utils.gather.utils.PyExecUtils;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.manager.utils.TestUtils;
import com.metoo.nrsm.core.service.*;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.core.utils.api.JindustryUnitRequest;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.utils.py.ssh.PythonExecUtils;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.vo.UnitVO;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.text.DecimalFormat;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RequestMapping("/admin/test")
@RestController
public class TestController {
    @Autowired
    private Ipv4DetailService ipV4DetailService;
    @Autowired
    private Ipv4Service ipv4Service;
    @Autowired
    private TestUtils testUtils;
    @Autowired
    private SnmpStatusUtils snmpStatusUtils;
    @Autowired
    private IUnitService unitService;
    @Autowired
    private ApiExecUtils apiExecUtils;
    @Autowired
    private ITerminalService terminalService;
    @Autowired
    private IGatherService gatherService;
    @Autowired
    private IDhcpService dhcpService;
    @Autowired
    private IDhcp6Service dhcp6Service;
    @Autowired
    private PythonExecUtils pythonExecUtils;



    // 周末和休息时间（17:周末和休息时间（17:30-8:30）按上述范围的1/10取值30-8:30）按上述范围的1/10取值
    public String getlow(double min, double max) {

        Random random = new Random();

        // Generate a random double between min (inclusive) and max (exclusive)
        double randomValue = min + (max - min) * random.nextDouble();

        // Divide randomValue by ten
        double result = randomValue;

        System.out.println("isWeekend()" + isWeekend());

        System.out.println("timeCheck()" + timeCheck());

        if(isWeekend() || timeCheck()){
            result = result / 10.0;
        }

        // Format result to two decimal places
        DecimalFormat df = new DecimalFormat("#.##");
        String formattedResult = df.format(result);
        return formattedResult;
    }

    // 封装判断是否为周末的方法
    public static boolean isWeekend() {
        LocalDate today = LocalDate.now();
        DayOfWeek dayOfWeek = today.getDayOfWeek();
        return dayOfWeek == DayOfWeek.SATURDAY || dayOfWeek == DayOfWeek.SUNDAY;
    }

    // 17:30-8:30

    @Test
    public void isInTime() {
//        LocalTime currentTime = LocalTime.now();
        LocalTime currentTime = LocalTime.of(8, 30);
        LocalTime endTime = LocalTime.of(17, 30);

        if (isAfter1730(currentTime) || isBefore830(currentTime)) {
            System.out.println("当前时间在17:30之后或8:30之前");
        } else {
            System.out.println("当前时间不在指定范围内");
        }
    }

    public boolean timeCheck(){
        LocalTime currentTime = LocalTime.now();
        if (isAfter1730(currentTime) || isBefore830(currentTime)) {
            System.out.println("当前时间在17:30之后或8:30之前");
            return true;
        } else {
            System.out.println("当前时间不在指定范围内");
            return false;
        }
    }

    public static boolean isAfter1730(LocalTime currentTime) {
        LocalTime endTime = LocalTime.of(17, 30);
        return currentTime.isAfter(endTime);
    }

    public static boolean isBefore830(LocalTime currentTime) {
        LocalTime startTime = LocalTime.of(8, 30);
        return currentTime.isBefore(startTime);
    }
}


