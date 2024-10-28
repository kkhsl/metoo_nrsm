package com.metoo.nrsm.entity;

import com.metoo.nrsm.core.domain.IdEntity;
import io.swagger.annotations.ApiModel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * @author HKK
 * @version 1.0
 * @date 2024-01-15 17:10
 */
@ApiModel("")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Dhcp6 extends IdEntity {


    private String dhcp;

    private String ia_na;
    private String cltt;
    private String iaaddr;
    private String binding_state;
    private String preferred_life;
    private String max_life;
    private String ends;

}
