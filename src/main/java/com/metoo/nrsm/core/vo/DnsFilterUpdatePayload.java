package com.metoo.nrsm.core.vo;

import com.metoo.nrsm.entity.DnsFilter;
import lombok.AllArgsConstructor;
import lombok.Data;

// 配置更新数据载体（新增类）
@Data
@AllArgsConstructor
public class DnsFilterUpdatePayload {
    private DnsFilter newConfig;
    private String oldDomain;
}