package com.metoo.nrsm.core.utils.gather.execpy;

import com.github.pagehelper.util.StringUtil;
import com.metoo.nrsm.core.service.IFluxConfigService;
import com.metoo.nrsm.core.utils.py.ssh.SSHExecutor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-04-01 12:40
 */
@Component
public class GgettrafficDemo {



    public static void main(String[] args) {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {"192.168.5.51", "v2c",
                "public@123", "1.3.6.1.2.1.31.1.1.1.6.770", "1.3.6.1.2.1.31.1.1.1.10.770"};

        SSHExecutor sshExecutor = new SSHExecutor();

        String result = sshExecutor.exec(path, params);
        if(StringUtil.isNotEmpty(result)){
            System.out.println(result);
        }
    }

    @Autowired
    private IFluxConfigService fluxConfigService;


//    public void gather(){
//
//        List<FluxConfig> fluxConfigs = this.fluxConfigService.selectObjByMap(null);
//        if(fluxConfigs.size() > 0){
//            // 获取全部ipv4流量
//            List<Map<String, String>> list = new ArrayList<>();
//            for (FluxConfig fluxConfig : fluxConfigs) {
//                // 获取ipv4流量
//                // 1. 遍历oid
//                Object[][] string2Array = JSON.parseObject(fluxConfig.getIpv4Oid(),Object[][].class);
//                for (int i = 0; i < string2Array.length; i++) {
//                    String in = String.valueOf(string2Array[i][0]);
//                    String out = String.valueOf(string2Array[i][1]);
//                    String result = test(fluxConfig.getIpv4(), in, out);
//                    if(MyStringUtils.isNotEmpty(result)){
//                        Map map = JSONObject.parseObject(result, Map.class);
//                        list.add(map);
//                    }
//                }
//            }
//
//            if(list.size() > 0){
//                BigDecimal in = list.stream().map(x ->
//                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
//                BigDecimal out = list.stream().map(x ->
//                        new BigDecimal(String.valueOf(x.get("in")))).reduce(BigDecimal.ZERO,BigDecimal::add);
//                BigDecimal ipv4Sum = in.add(out);
//                System.out.println(ipv4Sum);
//            }
//        }
//
//    }

    public String test(String ip, String in, String out) {
        String path = "/opt/nrsm/py/gettraffic.py";
        String[] params = {ip, "v2c",
                "public@123", in, out};
        SSHExecutor sshExecutor = new SSHExecutor();
        String result = sshExecutor.exec(path, params);
        if(StringUtil.isNotEmpty(result)){
            return result;
        }
        return null;
    }

}
