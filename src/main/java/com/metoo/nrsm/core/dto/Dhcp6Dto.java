package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Dhcp6;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class Dhcp6Dto extends PageDto<Dhcp6> {

    private String dhcp;

    private String name;
    private String ia_na;
    private String cltt;
    private String iaaddr;
    private String binding_state;
    private String preferred_life;
    private String max_life;
    private String ends;
}
