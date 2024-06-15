package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.AddressPoolFixed;
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
public class AddressPoolV6FixedDTO extends PageDto<AddressPoolFixed> {

    private Long id;

}
