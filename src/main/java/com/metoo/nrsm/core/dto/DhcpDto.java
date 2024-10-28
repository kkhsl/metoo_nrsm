package com.metoo.nrsm.core.dto;

import com.metoo.nrsm.core.dto.page.PageDto;
import com.metoo.nrsm.entity.Dhcp;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.Accessors;


@Data
@Accessors
@AllArgsConstructor
@NoArgsConstructor
public class DhcpDto extends PageDto<Dhcp> {

    private String dhcp;

    private String lease;
    private String starts;
    private String ends;
    private String tstp;
    private String cltt;
    private String binding_state;
    private String next_binding_state;
    private String rewind_binding_state;
    private String hardware_ethernet;
    private String uid;
    private String client_hostname;

}
