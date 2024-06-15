package com.metoo.nrsm.core.manager;

import com.alibaba.fastjson.JSONObject;
import com.metoo.nrsm.core.config.utils.ResponseUtil;
import com.metoo.nrsm.core.manager.utils.SystemInfoUtils;
import com.metoo.nrsm.core.manager.utils.TestUtils;
import com.metoo.nrsm.core.service.Ipv4DetailService;
import com.metoo.nrsm.core.service.Ipv4Service;
import com.metoo.nrsm.core.utils.ip.Ipv4Util;
import com.metoo.nrsm.core.vo.Result;
import com.metoo.nrsm.core.wsapi.utils.SnmpStatusUtils;
import com.metoo.nrsm.entity.Ipv4;
import com.metoo.nrsm.entity.Ipv4Detail;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

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

}
