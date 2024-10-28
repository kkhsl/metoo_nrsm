package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.JSONPath;
import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.config.ssh.utils.DateUtils;
import com.metoo.nrsm.core.config.utils.CopyPropertiesReflect;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.Gather;
import com.metoo.nrsm.core.config.utils.gather.factory.gather.GatherFactory;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.manager.utils.TestUtils;
import com.metoo.nrsm.core.service.ITerminalService;
import com.metoo.nrsm.core.service.IUnitService;
import com.metoo.nrsm.core.service.Ipv4DetailService;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.utils.Global;
import com.metoo.nrsm.core.utils.api.ApiExecUtils;
import com.metoo.nrsm.core.utils.api.ApiService;
import com.metoo.nrsm.core.utils.api.JindustryUnitRequest;
import com.metoo.nrsm.core.utils.date.DateTools;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.vo.UnitVO;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.*;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
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
    private ApiService apiService;
    @Autowired
    private ITerminalService terminalService;



    @GetMapping("/traffic")
    public void traffic(){
        GatherFactory factory = new GatherFactory();
        Gather gather = factory.getGather(Global.TRAFFIC);
        gather.executeMethod();
    }




    @GetMapping("/terminal/arp")
    public void termin_arp(){
        try {
//                this.terminalService.syncTerminal(date);
            this.terminalService.v4Tov6Terminal(new Date());
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 统计终端属于哪个单位
        //...
        try {
            this.terminalService.writeTerminalUnit();
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            this.terminalService.writeTerminalUnitV6();
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 根据vendor判断终端类型
        this.terminalService.writeTerminalType();

        // 统计终端是否属于双栈终端
        this.terminalService.dualStackTerminal();
    }


    @GetMapping("/terminal")
    public void termin(String value){
        if(StringUtil.isNotEmpty(value)){
            Map params = new HashMap();
            params.put("v6ip", value);
            List<Terminal> terminals = this.terminalService.selectObjByMap(params);
            if(terminals.size() > 0){
                Set<String> set = terminals.stream().map(e -> {
                    return e.getMac();
                }).collect(Collectors.toSet());
                System.out.println(set);
            }
        }
    }

    public static void main(String[] args) {
        try {
            //第二个为python脚本所在位置，后面的为所传参数（得是字符串类型）

            String[] args1 = new String[]{
                    "python", "E:\\python\\project\\djangoProject\\app01\\test.py"};

            // 创建新的字符串数组，长度为array1.length + array2.length
            String[] mergedArray = new String[args1.length + args.length];


            int args1Len = args1.length;

            for (int i = 0; i < mergedArray.length; i++) {

                if (i < args1Len) {
                    mergedArray[i] = args1[i];
                } else {
                    mergedArray[i] = args[i - args1Len];
                }
            }


            Process proc = Runtime.getRuntime().exec(mergedArray);// 执行py文件


//            Process proc = Runtime.getRuntime().exec(args1);// 执行py文件


//            String[] args1 = new String[] {
//                    "python", "E:\\python\\project\\djangoProject\\app01\\test.py"};
//            Process proc2 = Runtime.getRuntime().exec("\\n");// 执行回车
//            proc2.waitFor();

            BufferedReader in = new BufferedReader(new InputStreamReader(proc.getInputStream(), "gb2312"));//解决中文乱码，参数可传中文
            String line = null;
            while ((line = in.readLine()) != null) {
                System.out.println(line);
            }
            in.close();
            proc.waitFor();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }

    }

    @Test
    public void equalDate() throws InterruptedException {
        // 比较日期
        Date date = new Date();

        Date date1 = date;

        Date date2 = date;


        System.out.println(date1);

        Thread.sleep(100);

        Date date3 = new Date();

        System.out.println(date1.after(date2));

        System.out.println(date1.before(date2));

        System.out.println(date1.after(date3));

        System.out.println(date1.before(date3));

        System.out.println(date3);
    }

    @GetMapping("ipv4DetailsList")
    public void ipv4DetailsList() {

        Ipv4Detail ipv4DetailInit = this.ipV4DetailService.selectObjByIp("0.0.0.0");

        Map params = new HashMap();


        params.clear();
        List<Ipv4> ipv4List = this.ipv4Service.selectDuplicatesObjByMap(params);

        List<String> ips = ipv4List.stream().map(e -> Ipv4Util.ipConvertDec(e.getIp())).collect(Collectors.toList());

        params.clear();
        params.put("notId", ipv4DetailInit.getId());
        params.put("notIps", ips);
        List<Ipv4Detail> ipv4DetailsList = this.ipV4DetailService.selectObjByMap(params);
        System.out.println(ipv4DetailsList);
    }

    @GetMapping("sn")
    public String sn() {
        return SystemInfoUtils.getBiosUuid();
    }

    // 测试异常日志记录
    @GetMapping("/error")
    public Result testError() {
        try {
//            int i = 1 / 0;
            Map map = new HashMap();
            JSONObject host = JSONObject.parseObject(map.toString());
            String hostid = host.getString("hostid");
            return ResponseUtil.ok();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return ResponseUtil.error();
    }

    @GetMapping("testAdmin")
    public void testAdmin() {
        this.testUtils.test();
    }

    @GetMapping("testSnmp")
    public void testSnmp() {
        this.snmpStatusUtils.scanValue();
    }

    @GetMapping("/properties")
    public void properties(){
        Map params = new HashMap();
        params.put("hidden", true);
        List<Unit> units = this.unitService.selectObjByMap(params);

        UnitVO unitVO = new UnitVO();

        List<UnitVO> unitVos = new ArrayList<>();

        for (Unit unit : units) {
            CopyPropertiesReflect.copyPropertiesExceptId(unit, unitVO);
            unitVos.add(unitVO);
        }
        System.out.println(unitVos);
    }


    @GetMapping("/exec/gateway")
    public void apiExec(){
        this.apiExecUtils.exec2();
    }


    @GetMapping("/exec/unit")
    public void testExec(){
        this.exec();
    }

    public void exec(){

        // 采集流量数据，获取流量数据

        List<Map> list = new ArrayList();

        Map params = new HashMap();

        params.put("unitName", "鹰潭市文联");
        params.put("vfourFlow", getlow(3, 3.5));
        params.put("vsixFlow",  getlow(2, 3));
        params.put("date", DateUtils.getDateTime());
        params.put("time", System.currentTimeMillis());
        params.put("department", "YT-JJDS-WL");
        params.put("area", "360603");
        params.put("city", "360600");
        params.put("broadband_Account", "zww07013606022024080202");

        log.info("YT-JJDS-WL:" + "vfourFlow:" + getlow(3, 3.5));
        log.info("YT-JJDS-WL:" + "vsixFlow:" + getlow(2, 3) );


        Map params2 = new HashMap();

        params2.put("unitName", "鹰潭市供销社");
        params2.put("vfourFlow", getlow(5, 5.5));
        params2.put("vsixFlow",  getlow(3, 4));
        params2.put("date", DateUtils.getDateTime());
        params2.put("time", System.currentTimeMillis());
        params2.put("department", "YT-JJDS-GXS");
        params2.put("area", "360603");
        params2.put("city", "360600");
        params2.put("broadband_Account", "zww07013606022024080203");


        log.info("YT-JJDS-GXS:" + "vfourFlow:" + getlow(5, 5.5)  );
        log.info("YT-JJDS-GXS:" + "vsixFlow:" + getlow(3, 4) );


        Map params3 = new HashMap();

        params3.put("unitName", "鹰潭市老干部局");
        params3.put("vfourFlow", getlow(6, 6.5));
        params3.put("vsixFlow",  getlow(4, 5));
        params3.put("date", DateUtils.getDateTime());
        params3.put("time", System.currentTimeMillis());
        params3.put("department", "YT-JJDS-LGBJ");
        params3.put("area", "360602");
        params3.put("city", "360600");
        params3.put("broadband_Account", "zww07013606022024080201");


        log.info("YT-JJDS-LGBJ:" + "vfourFlow:" + getlow(6, 6.5)  );
        log.info("YT-JJDS-LGBJ:" + "vsixFlow:" + getlow(4, 5) );

        list.add(params);
        list.add(params2);
        list.add(params3);


        log.info("==========================list" + JSONObject.toJSONString(list));

        for (Map param : list) {
            String data = JSONObject.toJSONString(param);

            // 编码为 UTF-8
            byte[] utf8Bytes = data.getBytes(StandardCharsets.UTF_8);
            String utf8String = new String(utf8Bytes, StandardCharsets.UTF_8);

            JindustryUnitRequest jindustryUnitRequest = new JindustryUnitRequest();
            jindustryUnitRequest.setData(utf8String);
            jindustryUnitRequest.setNonce(UUID.randomUUID().toString());

            DateTools dateTools = new DateTools();

            jindustryUnitRequest.setTimestamp(dateTools.getTimestamp());

            // 监管平台接口
//            ApiService apiService = new ApiService(new RestTemplate());
//        http://47.120.66.93:19217/unit
//        http://59.52.34.196:6001/apisix/blade-ipv6/industryUnit 正式环境
//        http://47.120.66.93:19217/api/blade-ipv6/industryUnit
//            http://59.52.34.196:6001/apisix/blade-ipv6/industryUnit


        }

//        return JSONObject.toJSONString(list);
    }

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


