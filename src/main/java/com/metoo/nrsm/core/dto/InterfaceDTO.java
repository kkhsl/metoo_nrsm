package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.nspm.AddressPool;
import com.metoo.nrsm.entity.nspm.Interface;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;

import java.util.Date;

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
