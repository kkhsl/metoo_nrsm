package com.metoo.nrsm.core.config.utils.gather.factory.gather.utils;

import org.json.JSONArray;
import org.json.JSONObject;

/**
 * hk
 */
public class TrafficAnalyzerUtilsBack {


    public static void main(String[] args) {
        String jsonData = "[{'Vlan': '100', 'Direction': 'Inbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '10497595529', 'IPv6Bytes': '0'}, " +
                "{'Vlan': '100', 'Direction': 'Outbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '6716463687', 'IPv6Bytes': '0'}, " +
                "{'Vlan': '200', 'Direction': 'Inbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '32583387286', 'IPv6Bytes': '428540419'}, " +
                "{'Vlan': '200', 'Direction': 'Outbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '161483751704', 'IPv6Bytes': '0'}, " +
                "{'Vlan': '300', 'Direction': 'Inbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '314038407', 'IPv6Bytes': '8464052'}, " +
                "{'Vlan': '300', 'Direction': 'Outbound', 'IPv4': '3000', 'IPv6': '3000', 'IPv4Bytes': '861964134', 'IPv6Bytes': '0'}]";

        // 替换单引号为双引号来使 JSON 数据有效
        jsonData = jsonData.replace("'", "\"");

        // 获取 VLAN 100 的流量统计
        JSONObject result = getVlanTrafficStats(jsonData, "100");

        // 打印结果
        if (result != null) {
            System.out.println("Vlan " + result.getString("Vlan") + " IPv4 Traffic: " + result.getDouble("IPv4Traffic") + " MB");
            System.out.println("Vlan " + result.getString("Vlan") + " IPv6 Traffic: " + result.getDouble("IPv6Traffic") + " MB");
        } else {
            System.out.println("Vlan not found.");
        }
    }

    /**
     * 根据 JSON 数据和 VLAN 获取流量统计结果
     * @param jsonData JSON 格式的流量数据
     * @param vlan 需要查询的 VLAN
     * @return VLAN 的流量统计结果
     */
    public static JSONObject getVlanTrafficStats(String jsonData, String vlan) {
        // 解析 JSON 数据
        JSONArray jsonArray = new JSONArray(jsonData);

        // 使用 HashMap 存储每个 VLAN 的流量数据
        JSONObject vlanTraffic = new JSONObject();

        // 遍历 JSON 数组，按 VLAN 分类并计算 IPv4 和 IPv6 流量
        for (int i = 0; i < jsonArray.length(); i++) {
            JSONObject jsonObject = jsonArray.getJSONObject(i);

            String currentVlan = jsonObject.getString("Vlan");

            // 如果当前项是需要查询的 VLAN
            if (currentVlan.equals(vlan)) {
                long ipv4Bytes = jsonObject.getLong("IPv4Bytes");
                long ipv6Bytes = jsonObject.getLong("IPv6Bytes");

                // 如果没有初始化该 VLAN 的流量数据，则初始化
                if (!vlanTraffic.has(vlan)) {
                    vlanTraffic.put(vlan, new JSONObject());
                    vlanTraffic.getJSONObject(vlan).put("ipv4Bytes", 0L);
                    vlanTraffic.getJSONObject(vlan).put("ipv6Bytes", 0L);
                }

                // 累加流量
                vlanTraffic.getJSONObject(vlan).put("ipv4Bytes", vlanTraffic.getJSONObject(vlan).getLong("ipv4Bytes") + ipv4Bytes);
                vlanTraffic.getJSONObject(vlan).put("ipv6Bytes", vlanTraffic.getJSONObject(vlan).getLong("ipv6Bytes") + ipv6Bytes);
            }
        }

        // 如果找到指定 VLAN 的流量统计数据，则返回
        if (vlanTraffic.has(vlan)) {
            long totalIpv4Bytes = vlanTraffic.getJSONObject(vlan).getLong("ipv4Bytes");
            long totalIpv6Bytes = vlanTraffic.getJSONObject(vlan).getLong("ipv6Bytes");

            // 计算流量，单位为 MB
            double ipv4Traffic = totalIpv4Bytes / 1000000.0;
            double ipv6Traffic = totalIpv6Bytes / 1000000.0;

            // 返回计算结果
            JSONObject result = new JSONObject();
            result.put("Vlan", vlan);
            result.put("IPv4Traffic", ipv4Traffic);
            result.put("IPv6Traffic", ipv6Traffic);
            return result;
        }

        // 如果没有找到指定 VLAN，返回 null
        return null;
    }
}
