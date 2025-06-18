package com.metoo.nrsm.core.system.service.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ServiceInfo {

    private String name;      // 服务名称
    private String status;   // 服务状态描述
    private boolean active;  // 是否正在运行
    private boolean enabled; // 是否开机自启
}
