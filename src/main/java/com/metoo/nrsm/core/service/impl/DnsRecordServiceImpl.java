package com.metoo.nrsm.core.service.impl;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.convert.Convert;
import com.metoo.nrsm.core.mapper.DnsRecordMapper;
import com.metoo.nrsm.core.mapper.VendorMapper;
import com.metoo.nrsm.entity.DnsRecord;
import com.metoo.nrsm.entity.DnsTempLog;
import com.metoo.nrsm.core.service.IDnsLogService;
import com.metoo.nrsm.core.service.IDnsRecordService;
import com.metoo.nrsm.core.vo.DnsRecordVo;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 * dns记录服务
 *
 * @author zzy
 * @version 1.0
 * @date 2025/4/25 15:58
 */
@Service
@Slf4j
public class DnsRecordServiceImpl implements IDnsRecordService {
    @Resource
    private DnsRecordMapper dnsRecordMapper;
    @Resource
    private IDnsLogService dnsLogService;
    private static final int batchSize = 500;
    ;

    @Override
    public void saveRecord() {
        // 查询临时表数据
        List<DnsTempLog> recordInfo = dnsLogService.queryDnsLog();
        if (CollUtil.isNotEmpty(recordInfo)) {
            // 分批次提交
            for (int i = 0; i < recordInfo.size(); i += batchSize) {
                try {
                    List<DnsTempLog> subList = recordInfo.subList(i, Math.min(i + batchSize, recordInfo.size()));
                    // 保存到记录表中
                    List<DnsRecord> tempRecord = new ArrayList<>();
                    subList.forEach(o -> {
                        DnsRecord dnsRecord = Convert.convert(DnsRecord.class, o);
                        tempRecord.add(dnsRecord);
                    });
                    dnsRecordMapper.saveInfo(tempRecord);
                } catch (Exception e) {
                    log.error("批量插入dns汇总数据失败：{}", e);
                }
            }

        }
    }

    @Override
    public List<DnsRecordVo> ipv4TopN(String recordTime, Integer topN) {
        List<DnsRecordVo> result = new ArrayList<>();
        if (null == topN) {
            topN = 10;
        }
        List<DnsRecord> dnsRecords = dnsRecordMapper.queryRecordByTime(recordTime, topN);
        if (CollUtil.isNotEmpty(dnsRecords)) {
            dnsRecords.forEach(o -> {
                DnsRecordVo dnsRecord = Convert.convert(DnsRecordVo.class, o);
                result.add(dnsRecord);
            });
        }
        return result;
    }
}
