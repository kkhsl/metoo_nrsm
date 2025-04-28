package com.metoo.nrsm.core.service;

import com.metoo.nrsm.entity.DnsTempLog;

import java.util.List;

/**
 * dns解析服务
 * @author zzy
 * @version 1.0
 * @date 2025/4/25 15:55
 */
public interface IDnsLogService {
    /**
     * 解析日志
     * @return
     */
    void parseLargeLog();
    /**
     * 获取解析日志汇总数据（只有ipv4记录）
     * @return
     */
    List<DnsTempLog> queryDnsLog();

    /**
     * 清空临时日志表
     */
    void truncateTable();

    /**
     * 删除dns日志文件
     */
    void deleteDnsFile();
}