package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.AddressPoolFixed;
import com.metoo.nrsm.entity.AddressPoolIpv6;
import com.metoo.nrsm.entity.AddressPoolV6Fixed;
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
public class AddressPoolFixedDTO extends PageDto<AddressPoolV6Fixed> {

    private Long id;

}
