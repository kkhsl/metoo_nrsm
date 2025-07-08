package com.metoo.nrsm.core.monitor.core.schedule;

import com.metoo.nrsm.core.service.IDnsLogService;
import com.metoo.nrsm.core.service.IDnsRecordService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;

/**
 * 定时任务解析dns日志并保存汇总数据
 *
 * @author zzy
 * @version 1.0
 * @date 2025/4/27 10:02
 */
@Component
@Slf4j
public class AnalysisDnsLogTask {
    @Resource
    private IDnsLogService dnsLogService;
    @Resource
    private IDnsRecordService recordService;

    /**
     * 定时任务解析dns日志并保存汇总数据
     */
    @Scheduled(cron = "${dnsTask.cron}")
    public void analysisDnsLogTask() {
        log.info("====================================解析dns日志并保存汇总数据开始执行==========================");
        try {
            //删除之前的临时数据
            dnsLogService.truncateTable();
            // 解析日志文件并入库
            dnsLogService.parseLargeLog();
            // 获取解析的数据并汇总入库
            recordService.saveRecord();
            //删除日志文件
//            dnsLogService.deleteDnsFile();
        } catch (Exception e) {
            log.error("定时任务解析dns日志并保存汇总数据出现错误：{}", e);
        }
        log.info("====================================解析dns日志并保存汇总数据定时任务结束==========================");
    }

}
