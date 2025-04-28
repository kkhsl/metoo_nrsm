package com.metoo.nrsm.entity;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

/**
 * dns解析临时日志
 * @author zzy
 * @version 1.0
 * @date 2025/4/25 15:52
 */
@Builder
@Accessors(chain = true)
@Data
@AllArgsConstructor
@NoArgsConstructor
public class DnsTempLog {
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
    private Date logTime;

    private String ip;
    private String domainData;
    private String type;
    private Integer isCache;

    private String recordTime;
    private Integer num;
}
