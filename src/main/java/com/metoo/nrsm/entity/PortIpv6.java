package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-03-18 10:25
 */
@Data
@Accessors(chain = true)
@AllArgsConstructor
@NoArgsConstructor
public class PortIpv6 extends IdEntity {

    private String port;

    private String ipv6;

    private String deviceUuid;

    private Integer vlanNumber;

}
