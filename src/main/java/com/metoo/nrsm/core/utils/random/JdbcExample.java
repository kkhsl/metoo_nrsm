package com.metoo.nrsm.core.utils.random;


import com.metoo.nrsm.core.config.utils.gather.factory.gather.utils.GeneraFlowUtils;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

public class JdbcExample {

    public static void main(String[] args) {

        for (int i = 0; i < 30; i++) {
            double v6 = getV6();
            BigDecimal bd = BigDecimal.valueOf(v6);
            bd = bd.setScale(2, RoundingMode.HALF_UP);
            System.out.println(bd);
        }

    }

    public static double getV6() {
        String jdbcUrl = "jdbc:mysql://localhost:3306/nrsm";
        String username = "root";
        String password = "123456";

        String sql = "SELECT v4 FROM random";

        double totalV4 = 0;
        double totalV6 = 0;
        int count = 0;

        try (Connection connection = DriverManager.getConnection(jdbcUrl, username, password);
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {

            while (resultSet.next()) {

                double v4 = resultSet.getDouble("v4"); // 获取 v4 属性

                double v6 = GeneraFlowUtils.method4(v4, "20,30");

                totalV4 += v4; // 累加 v4
                totalV6 += v6; // 累加 v6
                count++;

            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        // 计算 v6 / (v4 + v6)
        if (count > 0) {
            double result = totalV6 / (totalV4 + totalV6);
            return result; // 返回计算结果
        }
        return 0;
    }
}
