package com.metoo.nrsm.core.network;

import net.ipip.ipdb.*;
import org.junit.Test;

import java.util.Arrays;
import java.util.Map;

public class TestIpdb {


    @Test
    public void test(){
//        try {
//            // City类可用于IPDB格式的IPv4免费库，
//            City db = new City("ipv4_china3_cn.ipdb");
//            // db.find(address, language) 返回索引数组
//            System.out.println(Arrays.toString(db.find("1.1.1.1", "CN")));
//            // db.findInfo(address, language) 返回 CityInfo 对象
//            CityInfo info = db.findInfo("119.28.1.1", "CN");
//            System.out.println("118.28.1.1输出如下：\t\n" + info);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//
//        try {
//            District db = new District("ipv4_china3_cn.ipdb");
//            System.out.println(Arrays.toString(db.find("1.12.13.1", "CN")));
//            DistrictInfo info = db.findInfo("1.12.13.1", "CN");
//            if (info != null) {
//                System.out.println(info);
//                System.out.println(info.getCountryName());
//            }
//            Map m = db.findMap("1.12.13.1", "CN");
//            System.out.println("1.12.13.1 输出如下：\t\n" + m);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//
//

        try {
            IDC db = new IDC("ipv4_china3_cn.ipdb");
//            System.out.println(Arrays.toString(db.find("1.1.1.1", "CN")));
//
//            IDCInfo info = db.findInfo("113.246.54.0", "CN");
//            System.out.println(info.getCountryName());


            Map m = db.findMap("113.246.54.0", "CN");
            System.out.println(m);

//            Map m2 = db.findMap("114.114.114.114", "CN");
//            System.out.println(m2);

        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
//            IDC db = new IDC("ipv4_china3_cn.ipdb");
//            System.out.println(Arrays.toString(db.find("1.1.1.1", "CN")));
//            IDCInfo info = db.findInfo("8.8.8.8", "CN");
//            System.out.println(info.getCountryName());
//            Map m = db.findMap("114.114.114.114", "CN");
//            System.out.println(m);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

//
//        try {
//            BaseStation db = new BaseStation("ipv4_china3_cn.ipdb");
//            System.out.println(Arrays.toString(db.find("1.68.1.255", "CN")));
//            System.out.println(db.findInfo("1.68.1.255", "CN"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }

    }
}
