package com.metoo.nrsm.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * dns 解析ipv4记录
 * @author zzy
 * @version 1.0
 * @date 2025/4/25 15:52
 */
@Builder
@Accessors(chain = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DnsRecord {
    private String recordTime;
    private String domainData;
    private Integer num;
}
