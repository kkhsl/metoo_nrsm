package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Interface;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-22 10:20
 */
@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class InterfaceDTO extends PageDto<Interface> {

    private String name;
    private String isup;
    private String ipv4address;
    private String ipv4netmask;
    private String ipv6address;
    private String ipv6netmask;
    private String macaddress;

}
