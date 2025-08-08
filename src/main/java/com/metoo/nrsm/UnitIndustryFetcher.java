package com.metoo.nrsm;

import org.springframework.http.*;
import org.springframework.web.client.RestTemplate;

import java.sql.*;
import java.util.*;

public class UnitIndustryFetcher {

    // 数据库连接配置
    private static final String JDBC_URL = "jdbc:mysql://192.168.7.102:3306/nrsm";
    private static final String USERNAME = "root";
    private static final String PASSWORD = "metoo89745000";
    private static final String API_URL = "http://175.6.37.154:10000/api/soft/version/syncBatchArea";

    /**
     * 主方法 - 程序入口
     */
    public static void main(String[] args) {
        // 获取所有单位数据
        List<UnitIndustry> units = fetchAllUnits();



        // 3. 转换为JSON并调用API
        callRemoteApiWithRestTemplate(units);

    }

    /**
     * 从数据库获取所有单位数据
     * @return 单位数据列表
     */
    public static List<UnitIndustry> fetchAllUnits() {
        List<UnitIndustry> unitList = new ArrayList<>();

        // 加载JDBC驱动（Java 6+可以省略这步）
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
        } catch (ClassNotFoundException e) {
            System.err.println("找不到MySQL JDBC驱动");
            e.printStackTrace();
            return unitList;
        }

        // SQL查询语句
        String sql = "SELECT id, unit_name, city_name, area_name FROM t_unit_industry";

        // 使用try-with-resources自动关闭资源
        try (Connection conn = DriverManager.getConnection(JDBC_URL, USERNAME, PASSWORD);
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            // 遍历结果集
            while (rs.next()) {
                UnitIndustry unit = new UnitIndustry();
                unit.setId(rs.getInt("id"));
                unit.setUnit(rs.getString("unit_name"));
                unit.setCity(rs.getString("city_name"));
                unit.setArea(rs.getString("area_name"));
                unitList.add(unit);
            }

        } catch (SQLException e) {
            System.err.println("数据库操作异常");
            e.printStackTrace();
        }

        return unitList;
    }

    /**
     * 使用RestTemplate调用远程API
     */
    public static void callRemoteApiWithRestTemplate(List<UnitIndustry> units) {
        RestTemplate restTemplate = new RestTemplate();

        // 设置请求头
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        // 创建请求实体
        HttpEntity<List<UnitIndustry>> requestEntity = new HttpEntity<>(units, headers);

        try {
            // 发送POST请求
            ResponseEntity<String> response = restTemplate.exchange(
                    API_URL,
                    HttpMethod.POST,
                    requestEntity,
                    String.class
            );

            // 处理响应
            System.out.println("API响应状态码: " + response.getStatusCodeValue());
            System.out.println("API响应内容: " + response.getBody());

        } catch (Exception e) {
            System.err.println("调用API时发生异常");
            e.printStackTrace();
        }
    }



    /**
     * 单位行业数据实体类
     */
    public static class UnitIndustry {
        private int id;
        private String unit;
        private String city;
        private String area;

        // 无参构造
        public UnitIndustry() {}

        // 全参构造
        public UnitIndustry(int id, String unit, String cityName, String areaName) {
            this.id = id;
            this.unit = unit;
            this.city = city;
            this.area = area;
        }

        // Getter和Setter方法
        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getUnit() {
            return unit;
        }

        public void setUnit(String unit) {
            this.unit = unit;
        }

        public String getCity() {
            return city;
        }

        public void setCity(String city) {
            this.city = city;
        }

        public String getArea() {
            return area;
        }

        public void setArea(String area) {
            this.area = area;
        }

        @Override
        public String toString() {
            return String.format(
                    "UnitIndustry{id=%d, unit='%s', city='%s', area='%s'}",
                    id, unit, city, area
            );
        }
    }
}
