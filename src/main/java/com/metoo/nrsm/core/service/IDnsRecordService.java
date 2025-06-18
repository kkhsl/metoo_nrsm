package com.metoo.nrsm.core.service;

import com.metoo.nrsm.core.vo.DnsRecordVo;

import java.util.List;

/**
 * dns解析服务
 * @author zzy
 * @version 1.0
 * @date 2025/4/25 15:55
 */
public interface IDnsRecordService {
    /**
     * 解析临时文件数据到记录表
     * @return
     */
    void saveRecord();

    List<DnsRecordVo> ipv4TopN(String recordTime, Integer topN);
}
