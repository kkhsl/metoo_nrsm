package com.metoo.nrsm.core.common;

/**
 * 公共变量
 *
 * @author zzy
 * @version 1.0
 * @date 2024/11/21 20:13
 */
public interface FlowConstants {
    /**
     * 日
     */
    String STATS_DIMENSION_DAY = "1";
    /**
     * 月
     */
    String STATS_DIMENSION_MONTH = "2";
    /**
     * 年
     */
    String STATS_DIMENSION_YEAR = "3";

    /**
     * 周
     */
    String STATS_DIMENSION_WEEK = "4";
    /**
     * ipv4名称
     */
    String IPV4 = "IPV4流量";

    /**
     * ipv6名称
     */
    String IPV6 = "IPV6流量";
    /**
     * ipv6Radio名称
     */
    String IPV6RADIO = "IPV6占比";
    /**
     * 单位在线天数
     */
    String DICT_ONLINE_DAY = "DICT_ONLINE_DAY";
    /**
     * 月达标比例
     */
    String DICT_MONTH_RADIO = "DICT_MONTH_RADIO";
    /**
     * 日达标比例
     */
    String DICT_DAY_RADIO = "DICT_DAY_RADIO";

}
